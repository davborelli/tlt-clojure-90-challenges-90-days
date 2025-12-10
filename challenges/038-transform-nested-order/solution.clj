;; =============================================================================
;; 038 - TRANSFORM NESTED ORDER
;; Level: 8/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms a deeply nested external order format into a
;; flattened internal format suitable for database storage or business logic.
;; We use get-in to safely extract values from nested paths.
;;
;; The approach explicitly maps each nested field to a flat key, making the
;; transformation clear and maintainable. We also coerce the total from string
;; to double, showing how adapters often combine structure transformation with
;; type coercion.
;;
;; This pattern is ubiquitous in production systems that integrate with external
;; APIs or legacy systems where data structures don't match internal models.

(ns challenge-038.solution)

;; IMPLEMENTATION
;; --------------

(defn transform-order
  "Transforms nested external order to flat internal format.

  Parameters:
  - external-order: Nested order structure from external system

  Returns: Flattened order map with typed values"
  [external-order]
  {:id              (:order-id external-order)
   :customer-name   (get-in external-order [:customer :personal :name])
   :customer-email  (get-in external-order [:customer :personal :email])
   :shipping-street (get-in external-order [:customer :shipping :address :street])
   :shipping-city   (get-in external-order [:customer :shipping :address :city])
   :shipping-zip    (get-in external-order [:customer :shipping :address :zip])
   :total           (Double/parseDouble (:total external-order))})

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. get-in for Safe Nested Access
;;    get-in takes a path vector and safely navigates nested maps.
;;    Example: (get-in {:a {:b {:c 42}}} [:a :b :c]) => 42
;;    If any key in the path doesn't exist, returns nil (doesn't throw).
;;    This is safer than chaining (:c (:b (:a data))) which is harder to read.
;;
;; 2. Flattening Nested Structures
;;    External systems often use deep nesting for organization (customer >
;;    personal > name), but internal systems prefer flat structures for easier
;;    querying and processing. This adapter bridges these two representations.
;;
;; 3. Combining Structure and Type Transformation
;;    Adapters often do two jobs simultaneously:
;;    1) Restructure (nested → flat, rename keys)
;;    2) Type coercion (string "99.50" → double 99.5)
;;    This makes them the perfect place for wire→domain transformations.
;;
;; 4. Explicit Field Mapping
;;    While we could use reduce-kv or other functional approaches, explicitly
;;    mapping each field makes the transformation obvious and maintainable.
;;    When fields have different transformations (some nested, some not, some
;;    with type coercion), explicit mapping is clearer than clever code.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Nested structure transformation with get-in
;;
;; Real-world usage: The reference code shows similar nested extraction:
;;   (get-in review [:customer :id])
;;   (get-in review [:request-info :device-info])
;;   (get-in token [:payload :principal :id])
;;
;; These demonstrate how production adapters extract data from complex nested
;; structures received from external services. The pattern ensures safe access
;; (returns nil rather than throwing) and makes data flow explicit.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Complete order transformation
  (transform-order
    {:order-id "ORD-123"
     :customer {:personal {:name "John Doe" :email "john@example.com"}
                :shipping {:address {:street "123 Main St"
                                     :city "Springfield"
                                     :zip "12345"}}}
     :total "99.50"})
  ;; => {:id "ORD-123"
  ;;     :customer-name "John Doe"
  ;;     :customer-email "john@example.com"
  ;;     :shipping-street "123 Main St"
  ;;     :shipping-city "Springfield"
  ;;     :shipping-zip "12345"
  ;;     :total 99.5}

  ;; Example 2: Different order
  (transform-order
    {:order-id "ORD-456"
     :customer {:personal {:name "Jane Smith" :email "jane@example.com"}
                :shipping {:address {:street "456 Oak Ave"
                                     :city "Portland"
                                     :zip "97201"}}}
     :total "149.99"})
  ;; => {:id "ORD-456"
  ;;     :customer-name "Jane Smith"
  ;;     :customer-email "jane@example.com"
  ;;     :shipping-street "456 Oak Ave"
  ;;     :shipping-city "Portland"
  ;;     :shipping-zip "97201"
  ;;     :total 149.99}
)

;; TESTS
;; -----

(defn -test []
  (let [result (transform-order
                 {:order-id "ORD-123"
                  :customer {:personal {:name "John Doe" :email "john@example.com"}
                             :shipping {:address {:street "123 Main St"
                                                  :city "Springfield"
                                                  :zip "12345"}}}
                  :total "99.50"})]
    (assert (= (:id result) "ORD-123")
            "Should extract order ID")
    (assert (= (:customer-name result) "John Doe")
            "Should extract customer name")
    (assert (= (:customer-email result) "john@example.com")
            "Should extract customer email")
    (assert (= (:shipping-street result) "123 Main St")
            "Should extract shipping street")
    (assert (= (:shipping-city result) "Springfield")
            "Should extract shipping city")
    (assert (= (:shipping-zip result) "12345")
            "Should extract shipping zip")
    (assert (= (:total result) 99.5)
            "Should convert total to double"))

  ;; Test another order
  (let [result (transform-order
                 {:order-id "ORD-456"
                  :customer {:personal {:name "Jane Smith" :email "jane@example.com"}
                             :shipping {:address {:street "456 Oak Ave"
                                                  :city "Portland"
                                                  :zip "97201"}}}
                  :total "149.99"})]
    (assert (= (:customer-name result) "Jane Smith")
            "Should handle different customer")
    (assert (= (:total result) 149.99)
            "Should handle different total"))

  (println "✓ All tests passed!"))

;; Run: (-test)
