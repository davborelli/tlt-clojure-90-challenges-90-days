;; =============================================================================
;; 060 - ORDER FULFILLMENT
;; Level: 12/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates complex order fulfillment orchestration using
;; threading macros. The process involves 7 sequential steps, each validating
;; conditions or enriching the order state with calculated values.
;;
;; We use -> to thread the order through all transformations, making the
;; workflow explicit and readable. Each helper is a pure transformation except
;; validation which may return an error. We short-circuit on validation failure.
;;
;; This pattern is fundamental in e-commerce and order management systems:
;; complex workflows are decomposed into focused steps that each handle one
;; aspect of the business process. The pipeline composes them into complete
;; order fulfillment logic.

(ns challenge-060.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn validate-order
  "Validates order has required fields.

  Parameters:
  - state: Order state

  Returns: State with :validated true, or error map"
  [state]
  (cond
    (not (:order-id state))
    {:status :error :message "Order must have order-id"}

    (not (:customer-id state))
    {:status :error :message "Order must have customer-id"}

    (or (not (:items state)) (empty? (:items state)))
    {:status :error :message "Order must have items"}

    (not (:shipping-address state))
    {:status :error :message "Order must have shipping address"}

    :else
    (assoc state :validated true)))

(defn check-inventory
  "Simulates inventory check (always succeeds for simplicity).

  Parameters:
  - state: Order state

  Returns: State with :inventory-reserved true"
  [state]
  (assoc state :inventory-reserved true))

(defn calculate-subtotal
  "Calculates order subtotal from items.

  Parameters:
  - state: Order state with :items

  Returns: State with :subtotal added"
  [state]
  (let [items (:items state)
        subtotal (reduce + (map #(* (:quantity %) (:unit-price %)) items))]
    (assoc state :subtotal subtotal)))

(defn calculate-shipping
  "Calculates shipping cost based on subtotal.

  Rules:
  - Free shipping if subtotal >= 100
  - $10 shipping if subtotal < 100

  Parameters:
  - state: Order state with :subtotal

  Returns: State with :shipping-cost added"
  [state]
  (let [shipping (if (>= (:subtotal state) 100) 0.0 10.0)]
    (assoc state :shipping-cost shipping)))

(defn calculate-total
  "Calculates final total (subtotal + shipping).

  Parameters:
  - state: Order state with :subtotal and :shipping-cost

  Returns: State with :total added"
  [state]
  (let [total (+ (:subtotal state) (:shipping-cost state))]
    (assoc state :total total)))

(defn generate-invoice
  "Generates invoice ID.

  Parameters:
  - state: Order state with :order-id

  Returns: State with :invoice-id added"
  [state]
  (let [invoice-id (str "INV-" (:order-id state))]
    (assoc state :invoice-id invoice-id)))

(defn mark-fulfilled
  "Marks order as fulfilled.

  Parameters:
  - state: Order state

  Returns: State with :status :fulfilled"
  [state]
  (assoc state :status :fulfilled))

;; MAIN CONTROLLER
;; ---------------

(defn fulfill-order
  "Orchestrates order fulfillment through multi-step pipeline.

  Pipeline steps:
  1. Validate order (required fields)
  2. Check inventory availability
  3. Calculate subtotal from items
  4. Calculate shipping cost
  5. Calculate final total
  6. Generate invoice ID
  7. Mark as fulfilled

  Parameters:
  - order-request: Initial order data

  Returns: Fulfilled order state or error map

  Uses -> threading for explicit, readable workflow."
  [order-request]
  (let [validated (validate-order order-request)]
    ;; Check if validation failed
    (if (= (:status validated) :error)
      validated  ; Return error immediately
      ;; Continue pipeline if validated
      (-> validated
          check-inventory
          calculate-subtotal
          calculate-shipping
          calculate-total
          generate-invoice
          mark-fulfilled))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-Step Order Fulfillment
;;    E-commerce order processing involves many steps:
;;    - Validation (required fields, valid products)
;;    - Inventory check (items in stock)
;;    - Pricing calculations (subtotal, taxes, shipping)
;;    - Reservation (reserve inventory)
;;    - Payment processing (charge customer)
;;    - Invoice generation (billing record)
;;    - Status update (mark as processing/fulfilled)
;;    Each step validates conditions and enriches order state.
;;
;; 2. State Enrichment Pipeline
;;    Each step adds fields to the order:
;;    - validate: adds :validated true
;;    - check-inventory: adds :inventory-reserved true
;;    - calculate-subtotal: adds :subtotal
;;    - calculate-shipping: adds :shipping-cost
;;    - calculate-total: adds :total
;;    - generate-invoice: adds :invoice-id
;;    - mark-fulfilled: updates :status to :fulfilled
;;    Final state has complete fulfillment record.
;;
;; 3. Business Rules in Calculations
;;    Shipping cost logic:
;;      - Free shipping for orders >= $100
;;      - $10 shipping for orders < $100
;;    This is a business rule. In production, rules might be more complex:
;;      - Different rates by region
;;      - Customer tier discounts
;;      - Promotional free shipping
;;    Centralizing rules in functions makes them easy to test and modify.
;;
;; 4. Fail-Fast Validation
;;    We validate first and short-circuit on error:
;;      (if validation-failed? error (-> state step1 step2 ...))
;;    This prevents wasted work: don't reserve inventory, calculate shipping,
;;    or generate invoices for invalid orders.
;;
;; 5. Pure Transformations for Testability
;;    Each helper is pure (no side effects):
;;    - No database writes
;;    - No API calls
;;    - No payment processing
;;    Side effects happen after pipeline (save to DB, send notifications).
;;    This makes testing trivial: call function, check output.
;;    In production, side effects would be in separate functions called
;;    after the pipeline succeeds.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo5.md
;;
;; Pattern used: Multi-step state enrichment pipeline
;;
;; Real-world usage: E-commerce fulfillment uses similar pipelines:
;;   (defn process-order [order]
;;     (-> order
;;         validate-order
;;         check-inventory
;;         validate-payment-method
;;         calculate-pricing
;;         reserve-inventory
;;         charge-payment
;;         generate-invoice
;;         send-confirmation-email
;;         mark-processing))
;;
;; The reference shows how production systems orchestrate complex workflows
;; using threading macros and focused, composable functions.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid order with free shipping
  (fulfill-order
    {:order-id "ORD-001"
     :customer-id "CUST-123"
     :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
             {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
     :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}})
  ;; => {:order-id "ORD-001"
  ;;     :customer-id "CUST-123"
  ;;     :items [...]
  ;;     :shipping-address {...}
  ;;     :validated true
  ;;     :inventory-reserved true
  ;;     :subtotal 110.0
  ;;     :shipping-cost 0.0  ; Free shipping (>= 100)
  ;;     :total 110.0
  ;;     :invoice-id "INV-ORD-001"
  ;;     :status :fulfilled}

  ;; Example 2: Valid order with shipping fee
  (fulfill-order
    {:order-id "ORD-002"
     :customer-id "CUST-456"
     :items [{:product-id "PROD-C" :quantity 1 :unit-price 50.00}]
     :shipping-address {:street "456 Oak Ave" :city "LA" :zip "90001"}})
  ;; => {:subtotal 50.0
  ;;     :shipping-cost 10.0  ; $10 shipping (< 100)
  ;;     :total 60.0}

  ;; Example 3: Invalid order (empty items)
  (fulfill-order
    {:order-id "ORD-003"
     :customer-id "CUST-789"
     :items []
     :shipping-address {:street "789 Pine St" :city "SF" :zip "94102"}})
  ;; => {:status :error :message "Order must have items"}

  ;; Example 4: Missing order-id
  (fulfill-order
    {:customer-id "CUST-999"
     :items [{:product-id "PROD-D" :quantity 1 :unit-price 100.00}]
     :shipping-address {:street "999 Elm St" :city "Austin" :zip "78701"}})
  ;; => {:status :error :message "Order must have order-id"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid order with free shipping
  (let [result (fulfill-order
                 {:order-id "ORD-001"
                  :customer-id "CUST-123"
                  :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
                          {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
                  :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}})]
    (assert (= (:order-id result) "ORD-001")
            "Should preserve order-id")
    (assert (true? (:validated result))
            "Should mark as validated")
    (assert (true? (:inventory-reserved result))
            "Should reserve inventory")
    (assert (= (:subtotal result) 110.0)
            "Should calculate subtotal (2*25 + 1*60)")
    (assert (= (:shipping-cost result) 0.0)
            "Should have free shipping for >= 100")
    (assert (= (:total result) 110.0)
            "Should calculate total (110 + 0)")
    (assert (= (:invoice-id result) "INV-ORD-001")
            "Should generate invoice ID")
    (assert (= (:status result) :fulfilled)
            "Should mark as fulfilled"))

  ;; Test order with shipping cost
  (let [result (fulfill-order
                 {:order-id "ORD-002"
                  :customer-id "CUST-456"
                  :items [{:product-id "PROD-C" :quantity 1 :unit-price 50.00}]
                  :shipping-address {:street "456 Oak Ave" :city "LA" :zip "90001"}})]
    (assert (= (:subtotal result) 50.0)
            "Should calculate subtotal")
    (assert (= (:shipping-cost result) 10.0)
            "Should charge $10 shipping for < 100")
    (assert (= (:total result) 60.0)
            "Should calculate total (50 + 10)"))

  ;; Test validation failures
  (assert (= (fulfill-order {:customer-id "CUST-789" :items [] :shipping-address {}})
             {:status :error :message "Order must have order-id"})
          "Should reject missing order-id")

  (assert (= (fulfill-order {:order-id "ORD-003" :items [] :shipping-address {}})
             {:status :error :message "Order must have customer-id"})
          "Should reject missing customer-id")

  (assert (= (fulfill-order {:order-id "ORD-004" :customer-id "CUST-999" :items [] :shipping-address {}})
             {:status :error :message "Order must have items"})
          "Should reject empty items")

  (assert (= (fulfill-order {:order-id "ORD-005" :customer-id "CUST-111" :items [{:product-id "P" :quantity 1 :unit-price 10}]})
             {:status :error :message "Order must have shipping address"})
          "Should reject missing shipping address")

  (println "✓ All tests passed!"))

;; Run: (-test)
