;; =============================================================================
;; 064 - MAP TO QUERY STRING (Bidirectional Part 2)
;; Level: 13/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms Clojure maps into URL query strings, the inverse
;; of Challenge 063. When making HTTP requests or building URLs, we need to
;; serialize parameters from internal maps to the string format expected by
;; web protocols.
;;
;; The approach converts each map entry to a "key=value" string, then joins
;; all pairs with &. We convert keyword keys to strings using `name`, keeping
;; the transformation simple and reversible.
;;
;; Together with Challenge 063, this forms a bidirectional transformation
;; enabling round-trip property testing: map → string → map should equal
;; the original map. This is critical for API integrations where data must
;; survive serialization and deserialization.

(ns challenge-064.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn map->query-string
  "Transforms Clojure map into URL query string format.

  Transformations:
  - Convert keyword keys to strings
  - Build key=value pairs
  - Join pairs with &

  Parameters:
  - params-map: Map with keyword keys and string values

  Returns: Query string (e.g., \"key1=value1&key2=value2\")"
  [params-map]
  (if (empty? params-map)
    ""
    (str/join "&"
              (map (fn [[k v]]
                     (str (name k) "=" v))
                   params-map))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Inverse Transformation
;;    This is the exact inverse of Challenge 063:
;;
;;    Challenge 063 (query-string->map):
;;      "name=John&age=25" → {:name "John" :age "25"}
;;
;;    Challenge 064 (map->query-string):
;;      {:name "John" :age "25"} → "name=John&age=25"
;;
;;    Inverse transformations enable bidirectional data flow between
;;    internal and external representations.
;;
;; 2. Building Strings with str/join
;;    Instead of manually concatenating with &:
;;      (str pair1 "&" pair2 "&" pair3)  ; Manual, error-prone
;;    We use join:
;;      (str/join "&" [pair1 pair2 pair3])  ; Clean, maintainable
;;    str/join handles empty collections gracefully and is more efficient
;;    for large collections (single pass, proper string builder usage).
;;
;; 3. Map Entry Destructuring
;;    When mapping over a map, each element is a key-value pair:
;;      (map (fn [[k v]] ...) {:a 1 :b 2})
;;                ^^^^^
;;                Destructures each entry
;;    This is cleaner than:
;;      (map (fn [entry] (let [k (key entry) v (val entry)] ...)) map)
;;
;; 4. Keyword to String Conversion
;;    Using `name` to convert keywords:
;;      (name :user-id) → "user-id"
;;      (name :status) → "status"
;;    `name` extracts the string part without the colon. This is reversible:
;;      (keyword "user-id") → :user-id
;;    Maintaining reversibility is crucial for round-trip transformations.
;;
;; 5. Map Ordering Caveat
;;    Maps are unordered in Clojure (hash maps). The query string parameter
;;    order may vary between calls:
;;      {:a "1" :b "2"} → "a=1&b=2" OR "b=2&a=1"
;;    For testing, check parsed equality, not string equality:
;;      (= (query-string->map result) expected-map)
;;    Most web servers treat query parameters as unordered.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Map to query string serialization
;;
;; Real-world usage: The reference shows building query strings for API calls:
;;   (defn build-api-url [base-url params]
;;     (str base-url "?" (map->query-string params)))
;;
;;   (build-api-url "https://api.example.com/search"
;;                  {:query "clojure" :page "1" :limit "10"})
;;   ;; => "https://api.example.com/search?query=clojure&page=1&limit=10"
;;
;; Production systems use this pattern for:
;; - HTTP client libraries (building GET requests)
;; - URL builders (search, pagination, filtering)
;; - Analytics tracking (UTM parameters)
;; - OAuth flows (authorization URLs)
;;
;; Together with challenge 063, enables full bidirectional transformation
;; for web API integrations.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Multiple parameters
  (map->query-string {:name "John" :age "25" :city "NYC"})
  ;; => "name=John&age=25&city=NYC"
  ;; (order may vary - maps are unordered)

  ;; Example 2: Boolean-like values
  (map->query-string {:status "active" :verified "true"})
  ;; => "status=active&verified=true"

  ;; Example 3: Empty map
  (map->query-string {})
  ;; => ""

  ;; Example 4: Single parameter
  (map->query-string {:query "clojure"})
  ;; => "query=clojure"

  ;; Example 5: Numeric-like values
  (map->query-string {:page "1" :limit "10" :offset "20"})
  ;; => "page=1&limit=10&offset=20"

  ;; Example 6: Round-trip test (requires challenge 063)
  ;; (require '[challenge-063.solution :refer [query-string->map]])
  ;;
  ;; (let [original-map {:name "John" :age "25" :city "NYC"}
  ;;       query-str (map->query-string original-map)
  ;;       parsed-map (query-string->map query-str)]
  ;;   (= original-map parsed-map))
  ;; ;; => true
  ;;
  ;; Round-trip property: (parse (serialize x)) = x
)

;; TESTS
;; -----

(defn -test []
  ;; Helper to parse and compare (since map order varies)
  (require '[challenge-063.solution :refer [query-string->map]])

  ;; Test multiple parameters (check by parsing back)
  (let [result (map->query-string {:name "John" :age "25" :city "NYC"})]
    (assert (= (query-string->map result)
               {:name "John" :age "25" :city "NYC"})
            "Should generate valid query string with multiple parameters"))

  ;; Test boolean-like values
  (let [result (map->query-string {:status "active" :verified "true"})]
    (assert (= (query-string->map result)
               {:status "active" :verified "true"})
            "Should handle boolean-like string values"))

  ;; Test empty map
  (assert (= (map->query-string {})
             "")
          "Should return empty string for empty map")

  ;; Test single parameter
  (assert (= (map->query-string {:query "clojure"})
             "query=clojure")
          "Should generate query string with single parameter")

  ;; Test numeric-like values
  (let [result (map->query-string {:page "1" :limit "10" :offset "20"})]
    (assert (= (query-string->map result)
               {:page "1" :limit "10" :offset "20"})
            "Should handle numeric-like string values"))

  ;; Round-trip test
  (let [original {:name "Alice" :email "alice@example.com" :role "admin"}
        round-trip (-> original map->query-string query-string->map)]
    (assert (= original round-trip)
            "Should satisfy round-trip property: map -> string -> map = original"))

  (println "✓ All tests passed! The map->query-string function works correctly."))

;; Run: (-test)
