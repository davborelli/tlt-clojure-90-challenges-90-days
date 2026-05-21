(ns payment-pipeline)

(def TWENTY-PERCENT 0.02)

(defn validate-payment
  [{:keys [amount] :as data}]
  (cond
    (> amount 0) (assoc data :status :validated)
    :else {:status :error :message "Invalid amount"}))

(defn calculate-fee 
  [{:keys [amount] :as data}]
  (assoc data :fee (* amount TWENTY-PERCENT)))

(defn calculate-total
  [{:keys [amount fee] :as data}]
  (assoc data :total (+ amount fee)))

(defn generate-transaction-id
  [{:keys [user-id amount] :as data}]
  (assoc data :transaction-id (str "TXN-" user-id "-" (hash amount))))

(defn add-timestamp
  [data]
  (assoc data :timestamp "2024-01-15T10:30:00"))

(defn mark-completed
  [data]
  (assoc data :status :completed))

(defn process-payment
  [payment-request]
  (let [validated (validate-payment payment-request)]
    (if (= (:status validated) :error)
      validated
      (-> validated
          (validate-payment)
          (calculate-fee)
          (calculate-total)
          (generate-transaction-id)
          (add-timestamp)
          (mark-completed)))))

(println (process-payment
          {:user-id "USER-123"
           :amount 100.00
           :recipient "MERCHANT-456"}))

(println (process-payment
          {:user-id "USER-456"
           :amount 0
           :recipient "MERCHANT-789"}))

(defn- tst []
  (assert (=
           (:status (process-payment
                     {:user-id "USER-123"
                      :amount 100.00
                      :recipient "MERCHANT-456"}))
           :completed))
  
  (let [result (process-payment
                {:user-id "USER-123"
                 :amount 100.00
                 :recipient "MERCHANT-456"})]
    (assert (= (:user-id result) "USER-123"))
    (assert (= (:amount result) 100.0))
    (assert (= (:fee result) 2.0))
    (assert (= (:total result) 102.0))
    (assert (.startsWith (:transaction-id result) "TXN-USER-123-"))
    (assert (= (:status result) :completed)))

  (assert (=
(process-payment
  {:user-id "USER-456"
   :amount 0
   :recipient "MERCHANT-789"})
{:status :error :message "Invalid amount"}))

  "SUCCESS")

(tst)
