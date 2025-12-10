;; =============================================================================
;; 039 - EXTRACT AND TRANSFORM PRODUCT
;; Level: 8/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms nested product data from external suppliers into a
;; flattened internal format while calculating derived fields (discounted price).
;; We extract nested values, coerce types, and perform business logic.
;;
;; The approach uses get-in for safe extraction, type coercion for parsing,
;; and intermediate let bindings for the discount calculation. This separates
;; concerns: extraction → parsing → calculation → assembly.
;;
;; This pattern is common in e-commerce systems that integrate with multiple
;; suppliers, each with different data formats, requiring normalization before
;; storage or display.

(ns challenge-039.solution)

;; IMPLEMENTATION
;; --------------

(defn transform-product
  "Transforms nested product structure with computed fields.

  Parameters:
  - external-product: Nested product structure from supplier

  Returns: Flattened product with typed and computed fields"
  [external-product]
  (let [;; Extract and parse pricing
        base-price-str (get-in external-product [:pricing :base-price])
        discount-str (get-in external-product [:pricing :discount-percent])
        price (Double/parseDouble base-price-str)
        discount (Double/parseDouble discount-str)
        ;; Calculate discounted price
        discounted-price (* price (- 1 (/ discount 100.0)))
        ;; Extract and parse stock
        stock-str (get-in external-product [:inventory :stock])
        stock (Integer/parseInt stock-str)]
    {:id               (:product-id external-product)
     :name             (get-in external-product [:details :name])
     :category         (get-in external-product [:details :category])
     :price            price
     :discounted-price discounted-price
     :stock            stock
     :warehouse        (get-in external-product [:inventory :warehouse])}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Computed Fields in Adapters
;;    Adapters don't just copy data; they often compute derived values.
;;    Here we calculate discounted-price from base-price and discount-percent.
;;    This is appropriate in adapters because it's a simple transformation of
;;    input data, not complex business logic requiring external dependencies.
;;
;; 2. Multiple Type Coercions
;;    This adapter performs three different type coercions:
;;    - String → Double (base-price, discount-percent)
;;    - String → Integer (stock)
;;    - Calculation → Double (discounted-price)
;;    This shows how adapters normalize data types from external systems.
;;
;; 3. let for Intermediate Calculations
;;    Using let to bind intermediate values (price, discount) makes the
;;    discount calculation clear. Without it, we'd have nested parseDouble
;;    calls that are hard to read. This is a readability trade-off: more
;;    lines but clearer logic.
;;
;; 4. Division by 100.0 for Percentage
;;    The .0 in 100.0 forces floating-point division: (/ 20 100.0) = 0.2
;;    Without it, integer division would give 0: (/ 20 100) = 0
;;    Then 1 - 0.2 = 0.8, so price * 0.8 gives 20% discount correctly.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Nested extraction with computed fields
;;
;; Real-world usage: The reference code shows similar patterns:
;;   (let [device-id (get-in request-info [:device-info :id])
;;         customer-id (get-in review [:customer :id])
;;         computed-value (some-calculation device-id)]
;;     {:extracted-field device-id
;;      :computed-field computed-value})
;;
;; This demonstrates how production adapters extract nested data and compute
;; derived values during transformation. The pattern is essential when
;; integrating with external systems that don't provide pre-calculated fields
;; that internal systems need.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Electronics product with 10% discount
  (transform-product
    {:product-id "PROD-001"
     :details {:name "Laptop" :category "Electronics"}
     :pricing {:base-price "999.99" :discount-percent "10"}
     :inventory {:stock "50" :warehouse "NYC"}})
  ;; => {:id "PROD-001"
  ;;     :name "Laptop"
  ;;     :category "Electronics"
  ;;     :price 999.99
  ;;     :discounted-price 899.991  ; 999.99 * 0.9
  ;;     :stock 50
  ;;     :warehouse "NYC"}

  ;; Example 2: Accessory with 20% discount
  (transform-product
    {:product-id "PROD-002"
     :details {:name "Mouse" :category "Accessories"}
     :pricing {:base-price "29.99" :discount-percent "20"}
     :inventory {:stock "200" :warehouse "LA"}})
  ;; => {:id "PROD-002"
  ;;     :name "Mouse"
  ;;     :category "Accessories"
  ;;     :price 29.99
  ;;     :discounted-price 23.992  ; 29.99 * 0.8
  ;;     :stock 200
  ;;     :warehouse "LA"}

  ;; Example 3: No discount case
  (transform-product
    {:product-id "PROD-003"
     :details {:name "Keyboard" :category "Accessories"}
     :pricing {:base-price "79.99" :discount-percent "0"}
     :inventory {:stock "100" :warehouse "CHI"}})
  ;; => {:discounted-price 79.99}  ; Same as base price
)

;; TESTS
;; -----

(defn -test []
  (let [result (transform-product
                 {:product-id "PROD-001"
                  :details {:name "Laptop" :category "Electronics"}
                  :pricing {:base-price "999.99" :discount-percent "10"}
                  :inventory {:stock "50" :warehouse "NYC"}})]
    (assert (= (:id result) "PROD-001")
            "Should extract product ID")
    (assert (= (:name result) "Laptop")
            "Should extract name")
    (assert (= (:category result) "Electronics")
            "Should extract category")
    (assert (= (:price result) 999.99)
            "Should parse price as double")
    ;; Discount calculation: 999.99 * (1 - 10/100) = 999.99 * 0.9 = 899.991
    (assert (< (Math/abs (- (:discounted-price result) 899.991)) 0.01)
            "Should calculate 10% discount correctly")
    (assert (= (:stock result) 50)
            "Should parse stock as integer")
    (assert (= (:warehouse result) "NYC")
            "Should extract warehouse"))

  (let [result (transform-product
                 {:product-id "PROD-002"
                  :details {:name "Mouse" :category "Accessories"}
                  :pricing {:base-price "29.99" :discount-percent "20"}
                  :inventory {:stock "200" :warehouse "LA"}})]
    (assert (= (:name result) "Mouse")
            "Should handle different product")
    ;; 29.99 * (1 - 20/100) = 29.99 * 0.8 = 23.992
    (assert (< (Math/abs (- (:discounted-price result) 23.992)) 0.01)
            "Should calculate 20% discount correctly"))

  (println "✓ All tests passed!"))

;; Run: (-test)
