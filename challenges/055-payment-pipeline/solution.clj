;; =============================================================================
;; 055 - PAYMENT PIPELINE
;; Level: 11/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates state transformation through a pipeline using
;; the threading macro (->). Payment processing involves multiple sequential
;; steps, each transforming the payment state by adding fields or updating
;; status.
;;
;; We use -> to thread the state through all transformations, making the data
;; flow clear and readable. Each helper function is a pure transformation:
;; takes state, returns transformed state. The pipeline composes these
;; transformations into a complete payment processing flow.
;;
;; This pattern is fundamental in transaction processing systems: complex
;; workflows are implemented as pipelines of focused transformations, each
;; handling one aspect of the business logic.

(ns challenge-055.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn validate-payment
  "Validates payment request amount.

  Parameters:
  - state: Payment state

  Returns: State with :status :validated, or error state"
  [state]
  (if (> (:amount state) 0)
    (assoc state :status :validated)
    {:status :error :message "Invalid amount"}))

(defn calculate-fee
  "Calculates transaction fee (2% of amount).

  Parameters:
  - state: Payment state

  Returns: State with :fee added"
  [state]
  (let [fee (* (:amount state) 0.02)]
    (assoc state :fee fee)))

(defn calculate-total
  "Calculates total charge (amount + fee).

  Parameters:
  - state: Payment state

  Returns: State with :total added"
  [state]
  (let [total (+ (:amount state) (:fee state))]
    (assoc state :total total)))

(defn generate-transaction-id
  "Generates unique transaction ID.

  Parameters:
  - state: Payment state

  Returns: State with :transaction-id added"
  [state]
  (let [txn-id (str "TXN-" (:user-id state) "-" (hash (:amount state)))]
    (assoc state :transaction-id txn-id)))

(defn add-timestamp
  "Adds processing timestamp (simulated).

  Parameters:
  - state: Payment state

  Returns: State with :timestamp added"
  [state]
  (assoc state :timestamp "2024-01-15T10:30:00"))

(defn mark-completed
  "Marks transaction as completed.

  Parameters:
  - state: Payment state

  Returns: State with :status :completed"
  [state]
  (assoc state :status :completed))

;; MAIN CONTROLLER
;; ---------------

(defn process-payment
  "Processes payment through transformation pipeline.

  Pipeline steps:
  1. Validate payment (amount > 0)
  2. Calculate transaction fee (2%)
  3. Calculate total charge (amount + fee)
  4. Generate unique transaction ID
  5. Add processing timestamp
  6. Mark as completed

  Parameters:
  - payment-request: Initial payment request

  Returns: Final transaction state or error state

  Uses -> threading macro to make data flow explicit and readable."
  [payment-request]
  (let [validated (validate-payment payment-request)]
    ;; Check if validation failed
    (if (= (:status validated) :error)
      validated  ; Return error state immediately
      ;; Continue pipeline if validated
      (-> validated
          calculate-fee
          calculate-total
          generate-transaction-id
          add-timestamp
          mark-completed))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. State Transformation Pipeline
;;    Each step transforms the state:
;;      request → validate → add-fee → add-total → add-id → add-timestamp → complete
;;    Each transformation is pure: (state) → (transformed-state)
;;    No side effects, no external state, just data transformation.
;;
;; 2. Threading Macro (->)
;;    -> threads the value through transformations:
;;      (-> state f g h) expands to (h (g (f state)))
;;    Makes code read left-to-right, matching data flow:
;;      state → f → g → h
;;    More readable than nested calls: (h (g (f state)))
;;
;; 3. Fail-Fast Validation
;;    We validate first and short-circuit on error:
;;      (if validation-failed?
;;        error-state
;;        (-> state transform1 transform2 ...))
;;    This prevents wasted work: don't process invalid payments.
;;
;; 4. Incremental State Building
;;    Each step adds fields to state:
;;    - validate: adds :status :validated
;;    - calculate-fee: adds :fee
;;    - calculate-total: adds :total
;;    - generate-id: adds :transaction-id
;;    - add-timestamp: adds :timestamp
;;    - mark-completed: updates :status to :completed
;;    Final state has complete transaction record.
;;
;; 5. Pure Transformations
;;    Each helper is pure:
;;    - No I/O, no database calls, no API requests
;;    - Same input always produces same output
;;    - No side effects
;;    This makes testing trivial: call function, check output.
;;    Side effects (save to DB, send notification) happen after pipeline.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo5.md
;;
;; Pattern used: State transformation pipeline with threading macro
;;
;; Real-world usage: Production transaction processing uses similar pipelines:
;;   (defn process-transaction [request]
;;     (-> request
;;         validate-request
;;         check-fraud
;;         verify-balance
;;         apply-business-rules
;;         calculate-charges
;;         generate-ids
;;         add-metadata
;;         mark-pending))
;;
;; The reference shows how threading macros create readable, maintainable
;; workflows for complex business processes. Each step is focused, testable,
;; and composable.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid payment
  (process-payment
    {:user-id "USER-123"
     :amount 100.00
     :recipient "MERCHANT-456"})
  ;; => {:user-id "USER-123"
  ;;     :amount 100.0
  ;;     :recipient "MERCHANT-456"
  ;;     :fee 2.0
  ;;     :total 102.0
  ;;     :transaction-id "TXN-USER-123-..."
  ;;     :timestamp "2024-01-15T10:30:00"
  ;;     :status :completed}

  ;; Example 2: Invalid amount (zero)
  (process-payment
    {:user-id "USER-456"
     :amount 0
     :recipient "MERCHANT-789"})
  ;; => {:status :error :message "Invalid amount"}

  ;; Example 3: Invalid amount (negative)
  (process-payment
    {:user-id "USER-789"
     :amount -50.00
     :recipient "MERCHANT-123"})
  ;; => {:status :error :message "Invalid amount"}

  ;; Example 4: Large payment
  (process-payment
    {:user-id "USER-999"
     :amount 10000.00
     :recipient "MERCHANT-AAA"})
  ;; => {:fee 200.0, :total 10200.0}

  ;; Example 5: Testing individual steps
  (validate-payment {:amount 100})
  ;; => {:amount 100 :status :validated}

  (calculate-fee {:amount 100})
  ;; => {:amount 100 :fee 2.0}

  (calculate-total {:amount 100 :fee 2.0})
  ;; => {:amount 100 :fee 2.0 :total 102.0}
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid payment pipeline
  (let [result (process-payment
                 {:user-id "USER-123"
                  :amount 100.00
                  :recipient "MERCHANT-456"})]
    (assert (= (:user-id result) "USER-123")
            "Should preserve user-id")
    (assert (= (:amount result) 100.0)
            "Should preserve amount")
    (assert (= (:recipient result) "MERCHANT-456")
            "Should preserve recipient")
    (assert (= (:fee result) 2.0)
            "Should calculate 2% fee")
    (assert (= (:total result) 102.0)
            "Should calculate total (amount + fee)")
    (assert (string? (:transaction-id result))
            "Should generate transaction ID")
    (assert (= (:timestamp result) "2024-01-15T10:30:00")
            "Should add timestamp")
    (assert (= (:status result) :completed)
            "Should mark as completed"))

  ;; Test invalid amount (zero)
  (assert (= (process-payment {:user-id "USER-456" :amount 0 :recipient "MERCHANT-789"})
             {:status :error :message "Invalid amount"})
          "Should reject zero amount")

  ;; Test invalid amount (negative)
  (assert (= (process-payment {:user-id "USER-789" :amount -50.00 :recipient "MERCHANT-123"})
             {:status :error :message "Invalid amount"})
          "Should reject negative amount")

  ;; Test individual steps
  (assert (= (validate-payment {:amount 100})
             {:amount 100 :status :validated})
          "validate-payment should add validated status")

  (assert (= (calculate-fee {:amount 100})
             {:amount 100 :fee 2.0})
          "calculate-fee should add 2% fee")

  (assert (= (calculate-total {:amount 100 :fee 2.0})
             {:amount 100 :fee 2.0 :total 102.0})
          "calculate-total should sum amount and fee")

  (assert (= (mark-completed {:status :validated})
             {:status :completed})
          "mark-completed should update status")

  (println "✓ All tests passed!"))

;; Run: (-test)
