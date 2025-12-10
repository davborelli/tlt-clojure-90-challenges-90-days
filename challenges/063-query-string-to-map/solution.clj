;; =============================================================================
;; 063 - QUERY STRING TO MAP (Bidirectional Part 1)
;; Level: 13/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter parses URL query strings into Clojure maps. Query strings
;; are the standard way web applications receive parameters (e.g., in URLs like
;; example.com/search?query=clojure&page=1). We transform this string format
;; into idiomatic Clojure data structures.
;;
;; The approach splits the string twice: first by & to get pairs, then by =
;; to separate keys from values. We convert keys to keywords for idiomatic
;; Clojure usage, keeping values as strings (type parsing can happen later).
;;
;; This is Part 1 of a bidirectional transformation. Challenge 064 implements
;; the reverse (map → query-string), enabling round-trip transformations for
;; web API integrations.

(ns challenge-063.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn query-string->map
  "Parses URL query string into Clojure map with keyword keys.

  Transformations:
  - Split by & to get pairs
  - Split each pair by = to get key and value
  - Convert keys to keywords
  - Keep values as strings

  Parameters:
  - query-string: URL query parameters (e.g., \"key1=value1&key2=value2\")

  Returns: Map with keyword keys and string values"
  [query-string]
  (if (empty? query-string)
    {}
    (let [pairs (str/split query-string #"&")]
      (into {}
            (map (fn [pair]
                   (let [[k v] (str/split pair #"=")]
                     [(keyword k) v]))
                 pairs)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Query String Format
;;    Query strings encode parameters in URLs:
;;      example.com/search?name=John&age=25&city=NYC
;;                         ^^^^^^^^^^^^^^^^^^^^^^^^
;;                         This is the query string
;;    Format rules:
;;    - Starts with ? (not included in our input)
;;    - key=value pairs separated by &
;;    - Keys and values are URL-encoded (we assume no encoding for simplicity)
;;
;; 2. Two-Stage Splitting
;;    We split twice to parse the structure:
;;      "name=John&age=25"
;;      → ["name=John" "age=25"]        (split by &)
;;      → [["name" "John"] ["age" "25"]] (split each by =)
;;    This mirrors the hierarchical structure: pairs separated by &,
;;    each pair has key=value separated by =.
;;
;; 3. Building Maps with into
;;    Pattern:
;;      (into {} [[key1 val1] [key2 val2] ...])
;;      → {key1 val1 key2 val2}
;;    `into` efficiently builds maps from sequences of key-value pairs.
;;    This is more idiomatic than using `assoc` repeatedly.
;;
;; 4. Keyword vs String Keys
;;    We convert string keys to keywords:
;;      "name" → :name
;;    Why keywords?
;;    - Idiomatic Clojure (maps typically use keyword keys)
;;    - Faster lookups (keywords are cached/interned)
;;    - Better syntax (`:name` vs `"name"`)
;;    Values stay as strings (caller decides if "25" should be parsed to int).
;;
;; 5. Edge Case Handling
;;    Empty string → empty map:
;;      (query-string->map "") → {}
;;    This is defensive: empty query string is valid (no parameters).
;;    Without this check, splitting "" would create [""], leading to
;;    incorrect parsing.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Query string parsing with split and transformation
;;
;; Real-world usage: The reference shows parsing query strings for API requests:
;;   (defn parse-request-params [request]
;;     (-> request
;;         :query-string
;;         query-string->map))
;;
;; Production systems use this pattern for:
;; - Web API endpoints (parsing GET parameters)
;; - URL builders (constructing search/filter URLs)
;; - Form submission handlers
;; - Analytics tracking (parsing UTM parameters)
;;
;; This challenge pairs with 064 (map→query-string) for full bidirectional
;; transformation, enabling round-trip property testing.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Multiple parameters
  (query-string->map "name=John&age=25&city=NYC")
  ;; => {:name "John" :age "25" :city "NYC"}

  ;; Example 2: Boolean-like values (still strings)
  (query-string->map "status=active&verified=true")
  ;; => {:status "active" :verified "true"}

  ;; Example 3: Empty query string
  (query-string->map "")
  ;; => {}

  ;; Example 4: Single parameter
  (query-string->map "query=clojure")
  ;; => {:query "clojure"}

  ;; Example 5: Numeric-like values (still strings)
  (query-string->map "page=1&limit=10&offset=20")
  ;; => {:page "1" :limit "10" :offset "20"}

  ;; Example 6: Round-trip test (requires challenge 064)
  ;; (let [original-map {:name "John" :age "25"}
  ;;       query-str (map->query-string original-map)
  ;;       parsed-map (query-string->map query-str)]
  ;;   (= original-map parsed-map))
  ;; ;; => true
)

;; TESTS
;; -----

(defn -test []
  ;; Test multiple parameters
  (assert (= (query-string->map "name=John&age=25&city=NYC")
             {:name "John" :age "25" :city "NYC"})
          "Should parse multiple parameters")

  ;; Test boolean-like values
  (assert (= (query-string->map "status=active&verified=true")
             {:status "active" :verified "true"})
          "Should keep boolean-like values as strings")

  ;; Test empty string
  (assert (= (query-string->map "")
             {})
          "Should return empty map for empty string")

  ;; Test single parameter
  (assert (= (query-string->map "query=clojure")
             {:query "clojure"})
          "Should parse single parameter")

  ;; Test numeric-like values
  (assert (= (query-string->map "page=1&limit=10&offset=20")
             {:page "1" :limit "10" :offset "20"})
          "Should keep numeric-like values as strings")

  ;; Test various key names
  (assert (= (query-string->map "firstName=Alice&lastName=Smith&email=alice@example.com")
             {:firstName "Alice" :lastName "Smith" :email "alice@example.com"})
          "Should handle camelCase keys")

  (println "✓ All tests passed! The query-string->map function works correctly."))

;; Run: (-test)
