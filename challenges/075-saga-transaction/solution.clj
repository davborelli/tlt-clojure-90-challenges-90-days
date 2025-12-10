;; =============================================================================
;; 075 - SAGA TRANSACTION
;; Level: 15/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller implements the saga pattern for distributed transactions.
;; A saga executes steps sequentially, and if any step fails, it runs
;; compensating transactions to undo previous steps, maintaining consistency
;; across distributed services.

(ns challenge-075.solution
  (:require [clojure.string :as str]))

;; SAGA STEPS
;; ----------

(defn reserve-inventory
  "Reserves inventory for order items."
  [state]
  (if (str/includes? (:order-id state) "fail-inventory")
    {:error "Inventory unavailable"}
    (assoc state :inventory-reserved true)))

(defn charge-payment
  "Charges payment for order."
  [state]
  (if (str/includes? (:order-id state) "fail-payment")
    {:error "Payment failed"}
    (assoc state :payment-charged true)))

(defn create-shipment
  "Creates shipment for order."
  [state]
  (if (str/includes? (:order-id state) "fail-shipment")
    {:error "Shipment creation failed"}
    (assoc state :shipment-created true)))

(defn send-confirmation
  "Sends order confirmation email."
  [state]
  (assoc state :confirmation-sent true))

;; COMPENSATING TRANSACTIONS
;; --------------------------

(defn release-inventory [state] (assoc state :inventory-released true))
(defn refund-payment [state] (assoc state :payment-refunded true))
(defn cancel-shipment [state] (assoc state :shipment-cancelled true))

;; SAGA ORCHESTRATION
;; ------------------

(defn execute-saga-step
  "Executes a single saga step.

  Parameters:
  - state: Current state
  - step-fn: Step function to execute
  - step-name: Name for tracking

  Returns: {:success true :state ...} or {:success false :error ...}"
  [state step-fn step-name]
  (let [result (step-fn state)]
    (if (:error result)
      {:success false :error (:error result) :step step-name}
      {:success true :state result :step step-name})))

(defn run-compensations
  "Runs compensating transactions in reverse order.

  Parameters:
  - state: Current state
  - completed-steps: Vector of completed step names

  Returns: Vector of compensation names run"
  [state completed-steps]
  (let [compensation-map {"reserve-inventory" release-inventory
                          "charge-payment" refund-payment
                          "create-shipment" cancel-shipment}]
    (->> completed-steps
         reverse
         (keep (fn [step] (when (compensation-map step) step)))
         (reduce (fn [comps step]
                   (when-let [comp-fn (compensation-map step)]
                     (comp-fn state)
                     (conj comps (str "compensate-" step))))
                 []))))

(defn process-order
  "Processes order through saga workflow with automatic rollback on failure.

  Saga steps:
  1. Reserve inventory
  2. Charge payment
  3. Create shipment
  4. Send confirmation

  On failure, runs compensations in reverse.

  Parameters:
  - order-request: Order map

  Returns: Success or failure map with compensation details"
  [order-request]
  (let [steps [["reserve-inventory" reserve-inventory]
               ["charge-payment" charge-payment]
               ["create-shipment" create-shipment]
               ["send-confirmation" send-confirmation]]]
    (loop [state order-request
           remaining-steps steps
           completed []]
      (if (empty? remaining-steps)
        ;; All steps succeeded
        {:status :completed
         :order-id (:order-id state)
         :steps completed}
        ;; Execute next step
        (let [[step-name step-fn] (first remaining-steps)
              result (execute-saga-step state step-fn step-name)]
          (if (:success result)
            ;; Step succeeded, continue
            (recur (:state result)
                   (rest remaining-steps)
                   (conj completed step-name))
            ;; Step failed, run compensations
            (let [compensations (run-compensations state completed)]
              {:status :failed
               :failed-step step-name
               :compensations-run compensations
               :reason (:error result)})))))))

;; TESTS
;; -----

(defn -test []
  ;; Test successful saga
  (let [result (process-order {:order-id "ORD-123" :user-id "U1" :items [] :payment {}})]
    (assert (= (:status result) :completed) "Should complete successfully")
    (assert (= (count (:steps result)) 4) "Should have 4 steps"))

  ;; Test inventory failure
  (let [result (process-order {:order-id "ORD-fail-inventory" :user-id "U1" :items [] :payment {}})]
    (assert (= (:status result) :failed) "Should fail")
    (assert (= (:failed-step result) "reserve-inventory") "Should fail at inventory")
    (assert (empty? (:compensations-run result)) "Should have no compensations (first step)"))

  ;; Test payment failure (with compensation)
  (let [result (process-order {:order-id "ORD-fail-payment" :user-id "U1" :items [] :payment {}})]
    (assert (= (:status result) :failed) "Should fail")
    (assert (= (:failed-step result) "charge-payment") "Should fail at payment")
    (assert (= (count (:compensations-run result)) 1) "Should run inventory compensation"))

  ;; Test shipment failure (multiple compensations)
  (let [result (process-order {:order-id "ORD-fail-shipment" :user-id "U1" :items [] :payment {}})]
    (assert (= (:status result) :failed) "Should fail")
    (assert (= (:failed-step result) "create-shipment") "Should fail at shipment")
    (assert (= (count (:compensations-run result)) 2) "Should run 2 compensations"))

  (println "✓ All tests passed! The process-order saga function works correctly."))

;; Run: (-test)
