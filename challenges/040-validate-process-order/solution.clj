;; =============================================================================
;; 040 - VALIDATE AND PROCESS ORDER
;; Level: 8/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller orchestrates multiple validation steps before processing
;; an order. It uses or composition to check each validation in sequence,
;; returning the first error encountered or proceeding to success.
;;
;; The approach extracts each validation into its own helper function, making
;; the code testable and the validation logic clear. The or operator naturally
;; implements the "fail-fast" pattern: stop at first error.
;;
;; This pattern is fundamental in production controllers that must validate
;; complex inputs before expensive operations like database writes, payment
;; processing, or external API calls.

(ns challenge-040.solution)

;; IMPLEMENTATION
;; --------------

(defn validate-customer-id
  "Validates customer ID is positive."
  [order]
  (when-not (pos? (:customer-id order))
    {:status :error :message "Invalid customer ID"}))

(defn validate-product-id
  "Validates product ID is positive."
  [order]
  (when-not (pos? (:product-id order))
    {:status :error :message "Invalid product ID"}))

(defn validate-quantity
  "Validates quantity is between 1 and 100."
  [order]
  (let [qty (:quantity order)]
    (when-not (and (pos? qty) (<= qty 100))
      {:status :error :message "Invalid quantity"})))

(defn validate-total
  "Validates order total is at least 10.0."
  [order]
  (when (< (:total order) 10.0)
    {:status :error :message "Order total too low"}))

(defn process-order
  "Validates and processes an order through multiple checks.

  Parameters:
  - order: Map with :customer-id, :product-id, :quantity, :total

  Returns: Map with :status and either :message (error) or :order-id (success)"
  [order]
  (or
    ;; Validation 1: Customer ID
    (validate-customer-id order)
    ;; Validation 2: Product ID
    (validate-product-id order)
    ;; Validation 3: Quantity
    (validate-quantity order)
    ;; Validation 4: Total
    (validate-total order)
    ;; All validations passed - create order
    {:status :success
     :order-id (str "ORD-" (:customer-id order) "-" (:product-id order))}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Fail-Fast with or Composition
;;    The or operator evaluates expressions left to right, returning the first
;;    truthy value. In validation, this implements "fail-fast": as soon as a
;;    validation fails (returns error map), or returns that error and stops.
;;    This is efficient (doesn't run unnecessary checks) and user-friendly
;;    (shows the first problem, not all problems at once).
;;
;; 2. Single Responsibility Functions
;;    Each validation function has one job: check one condition and return
;;    error or nil. This makes them:
;;    - Easy to test in isolation
;;    - Easy to understand (name tells you what it checks)
;;    - Easy to reuse in other contexts
;;    - Easy to modify (changing one validation doesn't affect others)
;;
;; 3. when-not for Validation
;;    when-not returns nil if condition is true, body if false.
;;    This is perfect for validation: if data is valid (condition true),
;;    return nil (no error). If invalid (condition false), return error map.
;;    Example: (when-not (pos? x) error) reads naturally as "when x is not
;;    positive, return error".
;;
;; 4. Validation Order Matters
;;    We validate in logical order: IDs first (cheap checks), then quantity
;;    and total (slightly more complex). In production, you'd validate cheapest
;;    operations first to fail fast before expensive checks like database
;;    lookups or API calls.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: or composition for sequential validation with fail-fast
;;
;; Real-world usage: The reference code shows this pattern:
;;   (or (validate-input)
;;       (check-authorization)
;;       (process-operation))
;;
;; And also shows validation helpers:
;;   (when-not (valid? data)
;;     (ex/validation-error!))
;;
;; This demonstrates how production controllers validate inputs before
;; expensive operations. The pattern ensures data integrity and provides
;; clear error messages when validation fails, improving system reliability
;; and user experience.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid order
  (process-order {:customer-id 123 :product-id 456 :quantity 5 :total 99.99})
  ;; => {:status :success, :order-id "ORD-123-456"}

  ;; Example 2: Invalid customer ID
  (process-order {:customer-id -1 :product-id 456 :quantity 5 :total 99.99})
  ;; => {:status :error, :message "Invalid customer ID"}

  ;; Example 3: Invalid product ID
  (process-order {:customer-id 123 :product-id 0 :quantity 5 :total 99.99})
  ;; => {:status :error, :message "Invalid product ID"}

  ;; Example 4: Invalid quantity (too high)
  (process-order {:customer-id 123 :product-id 456 :quantity 150 :total 99.99})
  ;; => {:status :error, :message "Invalid quantity"}

  ;; Example 5: Invalid total (too low)
  (process-order {:customer-id 123 :product-id 456 :quantity 1 :total 5.00})
  ;; => {:status :error, :message "Order total too low"}

  ;; Example 6: Boundary cases
  (process-order {:customer-id 1 :product-id 1 :quantity 1 :total 10.0})
  ;; => {:status :success, :order-id "ORD-1-1"}  ; Minimum valid values

  (process-order {:customer-id 999 :product-id 999 :quantity 100 :total 9999.99})
  ;; => {:status :success, :order-id "ORD-999-999"}  ; Maximum valid values
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid order
  (assert (= (process-order {:customer-id 123 :product-id 456 :quantity 5 :total 99.99})
             {:status :success :order-id "ORD-123-456"})
          "Should process valid order")

  ;; Test invalid customer ID
  (assert (= (process-order {:customer-id -1 :product-id 456 :quantity 5 :total 99.99})
             {:status :error :message "Invalid customer ID"})
          "Should reject negative customer ID")

  ;; Test invalid product ID
  (assert (= (process-order {:customer-id 123 :product-id 0 :quantity 5 :total 99.99})
             {:status :error :message "Invalid product ID"})
          "Should reject zero product ID")

  ;; Test invalid quantity (too high)
  (assert (= (process-order {:customer-id 123 :product-id 456 :quantity 150 :total 99.99})
             {:status :error :message "Invalid quantity"})
          "Should reject quantity > 100")

  ;; Test invalid quantity (zero)
  (assert (= (process-order {:customer-id 123 :product-id 456 :quantity 0 :total 99.99})
             {:status :error :message "Invalid quantity"})
          "Should reject zero quantity")

  ;; Test invalid total
  (assert (= (process-order {:customer-id 123 :product-id 456 :quantity 1 :total 5.00})
             {:status :error :message "Order total too low"})
          "Should reject total < 10.0")

  ;; Test boundary cases
  (assert (= (:status (process-order {:customer-id 1 :product-id 1 :quantity 1 :total 10.0}))
             :success)
          "Should accept minimum valid values")
  (assert (= (:status (process-order {:customer-id 999 :product-id 999 :quantity 100 :total 9999.99}))
             :success)
          "Should accept maximum valid quantity")

  (println "✓ All tests passed!"))

;; Run: (-test)
