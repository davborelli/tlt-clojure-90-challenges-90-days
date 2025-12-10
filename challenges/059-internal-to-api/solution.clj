;; =============================================================================
;; 059 - INTERNAL TO API (Part 2 of Bidirectional Pair)
;; Level: 12/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms internal domain models into external API request
;; format. It's the inverse of Challenge 058: we build nested structures from
;; flat data, convert kebab-case to camelCase, and transform keyword enums to
;; string enums.
;;
;; The approach destructures the flat internal model and constructs the nested
;; API structure directly using map literals. We convert keywords to strings,
;; uppercasing currency codes and keeping categories lowercase.
;;
;; Together with Challenge 058, this forms a complete bidirectional transformation
;; enabling seamless communication with external APIs while keeping internal
;; models clean and idiomatic.

(ns challenge-059.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn internal->api
  "Transforms internal product model into API request format.

  Transformations:
  - kebab-case → camelCase
  - Flat → nested structure
  - Keyword enums → string enums
  - Build required nested paths

  Parameters:
  - internal-product: Internal domain model

  Returns: External API format"
  [internal-product]
  (let [{:keys [product-id name category price currency
                stock warehouse-id warehouse-location]} internal-product]
    {:productId product-id
     :productDetails {:productName name
                      ;; Category stays lowercase
                      :category (clojure.core/name category)
                      :pricing {:basePrice price
                                ;; Currency uppercased
                                :currency (str/upper-case (clojure.core/name currency))}}
     :inventory {:stockLevel stock
                 :warehouse {:id warehouse-id
                             :location warehouse-location}}}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Inverse Transformation
;;    This is the exact inverse of Challenge 058 (api->internal):
;;
;;    Challenge 058 (api->internal):
;;      camelCase → kebab-case
;;      Nested → flat
;;      String → keyword
;;
;;    Challenge 059 (internal->api):
;;      kebab-case → camelCase
;;      Flat → nested
;;      Keyword → string
;;
;;    Inverse transformations enable bidirectional data flow.
;;
;; 2. Building Nested Structures
;;    Instead of flattening (extract nested → flat), we build nesting:
;;      Flat: {:name "..." :price ...}
;;      Nested: {:productDetails {:productName "..."
;;                                 :pricing {:basePrice ...}}}
;;    We use map literals to construct the structure directly.
;;
;; 3. Keyword to String Conversion
;;    Keywords to strings for enums:
;;      (name :electronics) => "electronics"
;;      (name :usd) => "usd"
;;    `name` extracts the string name from keyword.
;;    For currency, we uppercase: (str/upper-case "usd") => "USD"
;;    Different fields may have different casing conventions.
;;
;; 4. Selective Field Transformation
;;    Not all transformations are uniform:
;;    - category: keyword → lowercase string
;;    - currency: keyword → UPPERCASE string
;;    - product-id: no transformation (already string)
;;    Each field follows the API's specific conventions.
;;
;; 5. Omitting Generated Fields
;;    We don't include :metadata in the request.
;;    The API server generates:
;;    - :timestamp (server time)
;;    - :version (managed by API)
;;    - :requestId (generated per request)
;;    Clients send only business data, servers add metadata.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Building nested structures for API requests
;;
;; Real-world usage: API integration transforms internal to external:
;;   (defn create-product! [internal-product]
;;     (let [api-request (internal->api internal-product)
;;           response (http/post (str api-url "/products") api-request)]
;;       (api->internal (:body response))))
;;
;;   (defn update-product! [product-id updates]
;;     (let [current (fetch-product product-id)
;;           updated (merge current updates)
;;           api-request (internal->api updated)]
;;       (http/put (str api-url "/products/" product-id) api-request)))
;;
;; The reference shows how bidirectional transformations enable full CRUD
;; operations with external APIs while maintaining clean internal models.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Electronics product
  (internal->api
    {:product-id "PROD-123"
     :name "Laptop"
     :category :electronics
     :price 999.99
     :currency :usd
     :stock 50
     :warehouse-id "WH-001"
     :warehouse-location "New York"})
  ;; => {:productId "PROD-123"
  ;;     :productDetails {:productName "Laptop"
  ;;                      :category "electronics"
  ;;                      :pricing {:basePrice 999.99 :currency "USD"}}
  ;;     :inventory {:stockLevel 50
  ;;                 :warehouse {:id "WH-001" :location "New York"}}}

  ;; Example 2: Furniture product with EUR currency
  (internal->api
    {:product-id "PROD-456"
     :name "Office Chair"
     :category :furniture
     :price 299.50
     :currency :eur
     :stock 120
     :warehouse-id "WH-002"
     :warehouse-location "London"})
  ;; => {:productId "PROD-456"
  ;;     :productDetails {:productName "Office Chair"
  ;;                      :category "furniture"
  ;;                      :pricing {:basePrice 299.5 :currency "EUR"}}
  ;;     :inventory {:stockLevel 120
  ;;                 :warehouse {:id "WH-002" :location "London"}}}

  ;; Example 3: Round-trip test (requires api->internal from challenge 058)
  ;; (require '[challenge-058.solution :refer [api->internal]])
  ;;
  ;; (def original-api {:productId "PROD-123"
  ;;                    :productDetails {...}
  ;;                    :inventory {...}
  ;;                    :metadata {...}})
  ;;
  ;; (def round-trip (-> original-api api->internal internal->api))
  ;;
  ;; ;; Should match original except for :metadata (which we don't send back)
  ;; (= (dissoc original-api :metadata) round-trip)
  ;; ;; => true
)

;; TESTS
;; -----

(defn -test []
  (let [result (internal->api
                 {:product-id "PROD-123"
                  :name "Laptop"
                  :category :electronics
                  :price 999.99
                  :currency :usd
                  :stock 50
                  :warehouse-id "WH-001"
                  :warehouse-location "New York"})]
    ;; Test top-level field
    (assert (= (:productId result) "PROD-123")
            "Should transform product-id to productId")

    ;; Test productDetails nesting
    (assert (= (get-in result [:productDetails :productName]) "Laptop")
            "Should nest name as productName in productDetails")
    (assert (= (get-in result [:productDetails :category]) "electronics")
            "Should nest category and convert to string")

    ;; Test pricing nesting
    (assert (= (get-in result [:productDetails :pricing :basePrice]) 999.99)
            "Should nest price as basePrice in pricing")
    (assert (= (get-in result [:productDetails :pricing :currency]) "USD")
            "Should nest currency and convert to uppercase string")

    ;; Test inventory nesting
    (assert (= (get-in result [:inventory :stockLevel]) 50)
            "Should nest stock as stockLevel")
    (assert (= (get-in result [:inventory :warehouse :id]) "WH-001")
            "Should nest warehouse-id in warehouse")
    (assert (= (get-in result [:inventory :warehouse :location]) "New York")
            "Should nest warehouse-location in warehouse"))

  ;; Test different product
  (let [result (internal->api
                 {:product-id "PROD-456"
                  :name "Office Chair"
                  :category :furniture
                  :price 299.50
                  :currency :eur
                  :stock 120
                  :warehouse-id "WH-002"
                  :warehouse-location "London"})]
    (assert (= (get-in result [:productDetails :category]) "furniture")
            "Should handle different category")
    (assert (= (get-in result [:productDetails :pricing :currency]) "EUR")
            "Should handle different currency with uppercase"))

  (println "✓ All tests passed!"))

;; Run: (-test)
