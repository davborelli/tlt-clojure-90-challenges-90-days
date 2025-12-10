;; =============================================================================
;; 058 - API TO INTERNAL (Part 1 of Bidirectional Pair)
;; Level: 12/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms external API responses into internal domain models.
;; The API uses camelCase keys, deeply nested structures, string enums, and
;; includes metadata we don't need. We transform this into a clean internal
;; model with kebab-case keys, keyword enums, and flat structure.
;;
;; The approach uses get-in to extract values from nested paths, converts
;; string enums to lowercase keywords, and assembles a flat internal model.
;; We discard metadata that's only relevant to the API layer.
;;
;; This is Part 1 of a bidirectional transformation pair. Challenge 059
;; implements the reverse (internal → API). Together they enable communication
;; with external systems while keeping internal models clean.

(ns challenge-058.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn api->internal
  "Transforms API response into internal product model.

  Transformations:
  - camelCase → kebab-case
  - Nested extraction and flattening
  - String enums → keyword enums (lowercase)
  - Discard metadata

  Parameters:
  - api-response: External API format

  Returns: Internal domain model"
  [api-response]
  {:product-id (:productId api-response)
   :name (get-in api-response [:productDetails :productName])
   :category (keyword (str/lower-case (get-in api-response [:productDetails :category])))
   :price (get-in api-response [:productDetails :pricing :basePrice])
   :currency (keyword (str/lower-case (get-in api-response [:productDetails :pricing :currency])))
   :stock (get-in api-response [:inventory :stockLevel])
   :warehouse-id (get-in api-response [:inventory :warehouse :id])
   :warehouse-location (get-in api-response [:inventory :warehouse :location])})

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. External vs Internal Representation
;;    External (API):
;;    - Uses API provider's conventions (often camelCase for JSON APIs)
;;    - Includes metadata (timestamps, versions, pagination)
;;    - May nest data differently than we need
;;    - Uses string enums (JSON has no keyword type)
;;
;;    Internal (Domain):
;;    - Uses our conventions (kebab-case, keyword enums)
;;    - Only includes business data (no API metadata)
;;    - Structured for our use cases
;;    - Optimized for our application logic
;;
;; 2. Flattening Nested API Responses
;;    APIs often nest data for organization:
;;      {:productDetails {:productName "..." :pricing {:basePrice ...}}}
;;    We flatten for convenience:
;;      {:name "..." :price ...}
;;    This makes internal code simpler (less get-in calls everywhere).
;;
;; 3. String to Keyword Conversion (with normalization)
;;    APIs send enums as strings: "USD", "electronics"
;;    We convert to lowercase keywords: :usd, :electronics
;;    Pattern:
;;      (keyword (str/lower-case "USD")) => :usd
;;    This ensures consistency (API might send "USD", "usd", "Usd").
;;
;; 4. Discarding Irrelevant Data
;;    API responses often include metadata:
;;    - :timestamp (useful for caching, not for business logic)
;;    - :version (useful for API evolution, not for domain)
;;    - :pagination (useful for listing, not for single items)
;;    Adapters filter out this noise, keeping domain models clean.
;;
;; 5. Bidirectional Transformation Pattern
;;    This challenge (058): API → Internal
;;    Next challenge (059): Internal → API
;;    Together they enable:
;;    - Consuming external APIs (receive data)
;;    - Calling external APIs (send data)
;;    While keeping internal models decoupled from external formats.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Complex nested extraction with normalization
;;
;; Real-world usage: API integration transforms external to internal:
;;   (defn fetch-product [product-id]
;;     (let [api-response (http/get (str api-url "/products/" product-id))
;;           internal-model (api->internal (:body api-response))]
;;       internal-model))
;;
;;   (defn update-product [product-id updates]
;;     (let [internal-model (fetch-product product-id)
;;           updated (merge internal-model updates)
;;           api-request (internal->api updated)]
;;       (http/put (str api-url "/products/" product-id) api-request)))
;;
;; The references show similar patterns for integrating with external systems
;; while maintaining clean internal domain models.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Electronics product
  (api->internal
    {:productId "PROD-123"
     :productDetails {:productName "Laptop"
                      :category "electronics"
                      :pricing {:basePrice 999.99 :currency "USD"}}
     :inventory {:stockLevel 50
                 :warehouse {:id "WH-001" :location "New York"}}
     :metadata {:timestamp "2024-01-15" :version 2}})
  ;; => {:product-id "PROD-123"
  ;;     :name "Laptop"
  ;;     :category :electronics
  ;;     :price 999.99
  ;;     :currency :usd
  ;;     :stock 50
  ;;     :warehouse-id "WH-001"
  ;;     :warehouse-location "New York"}

  ;; Example 2: Furniture product with different currency
  (api->internal
    {:productId "PROD-456"
     :productDetails {:productName "Office Chair"
                      :category "furniture"
                      :pricing {:basePrice 299.50 :currency "EUR"}}
     :inventory {:stockLevel 120
                 :warehouse {:id "WH-002" :location "London"}}
     :metadata {:timestamp "2024-01-16" :version 1}})
  ;; => {:product-id "PROD-456"
  ;;     :name "Office Chair"
  ;;     :category :furniture
  ;;     :price 299.5
  ;;     :currency :eur
  ;;     :stock 120
  ;;     :warehouse-id "WH-002"
  ;;     :warehouse-location "London"}
)

;; TESTS
;; -----

(defn -test []
  (let [result (api->internal
                 {:productId "PROD-123"
                  :productDetails {:productName "Laptop"
                                   :category "electronics"
                                   :pricing {:basePrice 999.99 :currency "USD"}}
                  :inventory {:stockLevel 50
                              :warehouse {:id "WH-001" :location "New York"}}
                  :metadata {:timestamp "2024-01-15" :version 2}})]
    ;; Test field extraction and renaming
    (assert (= (:product-id result) "PROD-123")
            "Should extract and rename productId")
    (assert (= (:name result) "Laptop")
            "Should extract nested productName")
    (assert (= (:category result) :electronics)
            "Should extract and convert category to keyword")
    (assert (= (:price result) 999.99)
            "Should extract nested basePrice")
    (assert (= (:currency result) :usd)
            "Should extract and convert currency to lowercase keyword")
    (assert (= (:stock result) 50)
            "Should extract nested stockLevel")
    (assert (= (:warehouse-id result) "WH-001")
            "Should extract nested warehouse id")
    (assert (= (:warehouse-location result) "New York")
            "Should extract nested warehouse location")
    (assert (nil? (:metadata result))
            "Should discard metadata"))

  ;; Test different product
  (let [result (api->internal
                 {:productId "PROD-456"
                  :productDetails {:productName "Office Chair"
                                   :category "furniture"
                                   :pricing {:basePrice 299.50 :currency "EUR"}}
                  :inventory {:stockLevel 120
                              :warehouse {:id "WH-002" :location "London"}}
                  :metadata {:timestamp "2024-01-16" :version 1}})]
    (assert (= (:category result) :furniture)
            "Should handle different category")
    (assert (= (:currency result) :eur)
            "Should handle different currency"))

  (println "✓ All tests passed!"))

;; Run: (-test)
