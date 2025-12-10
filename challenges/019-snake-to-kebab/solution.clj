;; =============================================================================
;; 019 - CONVERT SNAKE CASE TO KEBAB CASE
;; Level: 4/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates key transformation, a common task when integrating
;; systems with different naming conventions. Databases and legacy systems often
;; use snake_case, while Clojure idiomatically uses kebab-case.
;;
;; We use reduce-kv (reduce over key-value pairs) to build a new map with
;; transformed keys. For each key-value pair, we:
;; 1. Convert the keyword key to a string
;; 2. Replace underscores with hyphens
;; 3. Convert back to a keyword
;; 4. Add to the accumulator map with the original value
;;
;; An alternative approach using update-keys (Clojure 1.11+) would be simpler:
;; (update-keys data #(-> % name (str/replace "_" "-") keyword))
;; However, we use reduce-kv for compatibility with older Clojure versions.
;;
;; This pattern appears in adapter layers between databases, APIs, and domain
;; models where naming conventions differ.

(ns challenge-019.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn snake->kebab-key
  "Helper function that converts a single snake_case keyword to kebab-case.

  Process: keyword → string → replace _ with - → keyword

  Parameters:
  - k: Keyword in snake_case

  Returns: Keyword in kebab-case"
  [k]
  ;; Convert keyword to string, replace underscores, convert back
  (-> k
      name                        ; :first_name → "first_name"
      (str/replace "_" "-")       ; "first_name" → "first-name"
      keyword))                   ; "first-name" → :first-name

(defn snake->kebab
  "Transforms all keys in a map from snake_case to kebab-case.

  Uses reduce-kv to build a new map with transformed keys while
  preserving all values.

  Parameters:
  - data: Map with snake_case keyword keys

  Returns: Map with kebab-case keyword keys"
  [data]
  ;; Reduce over key-value pairs, transforming each key
  (reduce-kv (fn [acc k v]
               (assoc acc (snake->kebab-key k) v))
             {}
             data))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. reduce-kv Function
;;    reduce-kv is like reduce but specifically for associative collections
;;    (maps). It passes key and value separately to the reducing function.
;;
;;    Signature: (reduce-kv f init coll)
;;
;;    The function f receives: (f accumulator key value)
;;
;;    Examples:
;;    ; Sum all values in a map
;;    (reduce-kv (fn [sum k v] (+ sum v))
;;               0
;;               {:a 1 :b 2 :c 3})
;;    ; => 6
;;
;;    ; Build new map with transformed values
;;    (reduce-kv (fn [acc k v] (assoc acc k (* v 2)))
;;               {}
;;               {:a 1 :b 2})
;;    ; => {:a 2 :b 4}
;;
;;    ; Filter map by value
;;    (reduce-kv (fn [acc k v]
;;                 (if (even? v)
;;                   (assoc acc k v)
;;                   acc))
;;               {}
;;               {:a 1 :b 2 :c 3 :d 4})
;;    ; => {:b 2 :d 4}
;;
;; 2. name Function
;;    name converts keywords and symbols to strings, extracting just the name
;;    without the namespace or leading colon.
;;
;;    Examples:
;;    (name :hello)              ; => "hello"
;;    (name :user/id)            ; => "id" (drops namespace)
;;    (name 'my-symbol)          ; => "my-symbol"
;;    (name "already-string")    ; => "already-string"
;;
;;    Related functions:
;;    (namespace :user/id)       ; => "user"
;;    (keyword "hello")          ; => :hello
;;    (keyword "user" "id")      ; => :user/id
;;
;; 3. clojure.string/replace
;;    replace substitutes all occurrences of a pattern with a replacement.
;;
;;    Signatures:
;;    (str/replace s match replacement)  ; match is string or regex
;;
;;    Examples:
;;    (str/replace "hello_world" "_" "-")
;;    ; => "hello-world"
;;
;;    (str/replace "abc123def" #"\d+" "X")
;;    ; => "abcXdef"
;;
;;    (str/replace "  spaces  " #"\s+" " ")
;;    ; => " spaces "
;;
;;    Key points:
;;    - String match: replaces all occurrences
;;    - Regex match: powerful pattern matching
;;    - Original string unchanged (returns new string)
;;
;; 4. Naming Conventions in Clojure
;;    Different conventions for different contexts:
;;
;;    kebab-case (Clojure idiomatic):
;;    - Functions: my-function, process-data
;;    - Keywords: :first-name, :user-id
;;    - Namespaces: my-app.core
;;
;;    snake_case (databases, Python):
;;    - Database columns: user_id, created_at
;;    - Python variables: my_variable
;;
;;    camelCase (JavaScript, Java):
;;    - JavaScript: firstName, userId
;;    - Java methods: getName, setUserId
;;
;;    PascalCase (Java, C#):
;;    - Java classes: MyClass, UserService
;;    - C# types: MyType, UserModel
;;
;; 5. Alternative Approach with update-keys
;;    In Clojure 1.11+, update-keys makes this simpler:
;;
;;    (defn snake->kebab [data]
;;      (update-keys data
;;                   (fn [k]
;;                     (-> k
;;                         name
;;                         (str/replace "_" "-")
;;                         keyword))))
;;
;;    Or more concisely:
;;    (defn snake->kebab [data]
;;      (update-keys data #(-> % name (str/replace "_" "-") keyword)))
;;
;;    update-keys applies a function to all keys in a map.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Key naming convention transformation
;;
;; Real-world usage: The reference code shows key transformation:
;;   (->camelCase m) - Converting to camelCase for JavaScript APIs
;;   (map->query-string m) - Converting map keys to query string format
;;
;; In production systems, this appears in:
;; - Database adapters (snake_case columns → kebab-case domain)
;; - API adapters (camelCase JSON → kebab-case Clojure)
;; - Configuration loading (ENV_VAR format → kebab-case)
;; - Legacy system integration

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: User data from database
  (snake->kebab {:first_name "John" :last_name "Doe"})
  ;; => {:first-name "John" :last-name "Doe"}

  ;; Example 2: Multiple underscores
  (snake->kebab {:user_id 123 :email_address "test@example.com"})
  ;; => {:user-id 123 :email-address "test@example.com"}

  ;; Example 3: No underscores (unchanged)
  (snake->kebab {:name "Alice"})
  ;; => {:name "Alice"}

  ;; Example 4: Empty map
  (snake->kebab {})
  ;; => {}

  ;; Example 5: Three-word keys
  (snake->kebab {:created_at_timestamp 1234567890})
  ;; => {:created-at-timestamp 1234567890}

  ;; Example 6: Mixed keys
  (snake->kebab {:user_name "Bob" :age 25 :home_address "123 Main St"})
  ;; => {:user-name "Bob" :age 25 :home-address "123 Main St"}

  ;; Example 7: Using in database adapter
  (defn db-row->domain [row]
    (-> row
        (snake->kebab)
        (assoc :loaded-at (System/currentTimeMillis))))

  (db-row->domain {:user_id 1 :user_name "Alice"})
  ;; => {:user-id 1 :user-name "Alice" :loaded-at 1234567890123}

  ;; Example 8: Mapping over collection from database
  (map snake->kebab [{:first_name "John" :last_name "Doe"}
                     {:first_name "Jane" :last_name "Smith"}])
  ;; => ({:first-name "John" :last-name "Doe"}
  ;;     {:first-name "Jane" :last-name "Smith"})
)

;; TESTS
;; -----

(defn -test []
  ;; Test basic transformation
  (assert (= (snake->kebab {:first_name "John" :last_name "Doe"})
             {:first-name "John" :last-name "Doe"})
          "Should transform snake_case to kebab-case")

  ;; Test multiple underscores
  (assert (= (snake->kebab {:user_id 123 :email_address "test@example.com"})
             {:user-id 123 :email-address "test@example.com"})
          "Should handle multiple keys with underscores")

  ;; Test no underscores
  (assert (= (snake->kebab {:name "Alice"})
             {:name "Alice"})
          "Keys without underscores should remain unchanged")

  ;; Test empty map
  (assert (= (snake->kebab {})
             {})
          "Empty map should remain empty")

  ;; Test three-word key
  (assert (= (snake->kebab {:created_at_timestamp 1234567890})
             {:created-at-timestamp 1234567890})
          "Should handle multiple underscores in one key")

  ;; Test mixed keys
  (assert (= (snake->kebab {:user_name "Bob" :age 25 :home_address "123 Main St"})
             {:user-name "Bob" :age 25 :home-address "123 Main St"})
          "Should handle mix of keys with and without underscores")

  ;; Test values preserved
  (let [result (snake->kebab {:user_id 999 :active_flag true})]
    (assert (= (:user-id result) 999)
            "Integer values should be preserved")
    (assert (= (:active-flag result) true)
            "Boolean values should be preserved"))

  ;; Test helper function
  (assert (= (snake->kebab-key :first_name) :first-name)
          "Helper should convert single key")
  (assert (= (snake->kebab-key :no_change) :no-change)
          "Helper should replace underscores")

  (println "✓ All tests passed! The snake->kebab function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
