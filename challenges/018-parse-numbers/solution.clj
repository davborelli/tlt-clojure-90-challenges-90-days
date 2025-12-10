;; =============================================================================
;; 018 - PARSE STRING NUMBERS TO INTEGERS
;; Level: 4/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates type coercion, a critical pattern when integrating
;; with external systems. Data from HTTP APIs, web forms, CSV files, and
;; environment variables typically arrives as strings, even for numeric values.
;;
;; We use the threading macro (->) to chain multiple update operations, making
;; the transformation pipeline clear: start with the input map, parse age to
;; integer, then parse score to integer. Each update returns a new map with
;; one field transformed.
;;
;; This is more maintainable than manually creating a new map with assoc,
;; because update preserves all other fields automatically and clearly shows
;; which fields are being transformed.
;;
;; Type coercion patterns like this appear constantly in adapter layers that
;; sit between external data sources and internal domain models.

(ns challenge-018.solution)

;; IMPLEMENTATION
;; --------------

(defn parse-numbers
  "Transforms string numbers in a user map to integers.

  Converts :age and :score from strings to integers while keeping
  :name as a string.

  Parameters:
  - user-data: Map with :name, :age (string), and :score (string)

  Returns: Map with :age and :score as integers"
  [user-data]
  ;; Chain update operations using threading macro
  ;; Start with user-data, parse age, then parse score
  (-> user-data
      (update :age #(Integer/parseInt %))
      (update :score #(Integer/parseInt %))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Type Coercion
;;    Converting data from one type to another. Common coercions:
;;
;;    String → Integer:
;;    (Integer/parseInt "42")         ; => 42
;;    (Integer/parseInt "0")          ; => 0
;;
;;    String → Long:
;;    (Long/parseLong "12345678901")  ; => 12345678901
;;
;;    String → Double:
;;    (Double/parseDouble "3.14")     ; => 3.14
;;
;;    String → Boolean:
;;    (Boolean/parseBoolean "true")   ; => true
;;
;;    Integer → String:
;;    (str 42)                        ; => "42"
;;
;;    Key points:
;;    - Parse* functions throw on invalid input ("abc" → error)
;;    - Always validate strings before parsing in production
;;    - Choose type based on value range (Integer vs Long)
;;
;; 2. Integer/parseInt Java Interop
;;    Clojure runs on JVM and can call Java methods directly.
;;
;;    Syntax: (ClassName/staticMethod args)
;;
;;    Examples:
;;    (Integer/parseInt "100")        ; => 100
;;    (Math/abs -5)                   ; => 5
;;    (System/currentTimeMillis)      ; => 1234567890123
;;
;;    Common parsing methods:
;;    (Integer/parseInt "42")         ; String → int
;;    (Long/parseLong "99")           ; String → long
;;    (Double/parseDouble "3.14")     ; String → double
;;    (Float/parseFloat "2.5")        ; String → float
;;
;;    Error handling:
;;    (Integer/parseInt "abc")        ; Throws NumberFormatException
;;
;; 3. Threading with -> (thread-first)
;;    The -> macro threads a value through multiple functions, passing
;;    it as the FIRST argument to each function.
;;
;;    Syntax:
;;    (-> value
;;        (fn1 args)
;;        (fn2 args)
;;        (fn3 args))
;;
;;    Expands to:
;;    (fn3 (fn2 (fn1 value args) args) args)
;;
;;    Examples:
;;    (-> {:a 1}
;;        (assoc :b 2)
;;        (dissoc :a))
;;    ; => {:b 2}
;;
;;    (-> "hello"
;;        (str " world")
;;        (str/upper-case))
;;    ; => "HELLO WORLD"
;;
;;    (-> user
;;        (update :age inc)
;;        (assoc :status :active))
;;
;;    When to use ->:
;;    - Transforming a single value through pipeline
;;    - Working with maps (assoc, dissoc, update)
;;    - Improves readability (top-to-bottom flow)
;;
;; 4. Anonymous Functions in update
;;    We use #(Integer/parseInt %) as shorthand for:
;;    (fn [x] (Integer/parseInt x))
;;
;;    The #() reader macro creates anonymous functions:
;;    - % is the first argument
;;    - %1, %2, %3 for multiple arguments
;;    - %& for rest arguments
;;
;;    Examples:
;;    #(+ % 1)           ; (fn [x] (+ x 1))
;;    #(* % %)           ; (fn [x] (* x x))
;;    #(str %1 %2)       ; (fn [x y] (str x y))
;;
;;    In our case:
;;    (update :age #(Integer/parseInt %))
;;    ; Means: apply Integer/parseInt to the current :age value

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Type coercion in adapter layer
;;
;; Real-world usage: The reference code shows similar coercion patterns:
;;   (entry->record m) - Converting database types to domain types
;;   (record->entry m) - Converting domain types to database types
;;
;; In production systems, this appears in:
;; - API request parsing (query params, form data)
;; - Database adapters (string dates → timestamps)
;; - Configuration loading (env vars → numbers)
;; - CSV/Excel import (all fields are strings)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Standard user data
  (parse-numbers {:name "John" :age "25" :score "100"})
  ;; => {:name "John" :age 25 :score 100}

  ;; Example 2: Different values
  (parse-numbers {:name "Jane" :age "30" :score "95"})
  ;; => {:name "Jane" :age 30 :score 95}

  ;; Example 3: Edge case - zero score
  (parse-numbers {:name "Bob" :age "18" :score "0"})
  ;; => {:name "Bob" :age 18 :score 0}

  ;; Example 4: Large numbers
  (parse-numbers {:name "Alice" :age "99" :score "9999"})
  ;; => {:name "Alice" :age 99 :score 9999}

  ;; Example 5: Using result in calculations
  (let [user (parse-numbers {:name "Charlie" :age "25" :score "85"})]
    (if (>= (:score user) 90)
      "Pass"
      "Fail"))
  ;; => "Fail"

  ;; Example 6: Mapping over collection
  (map parse-numbers [{:name "Dave" :age "20" :score "70"}
                      {:name "Emma" :age "22" :score "80"}])
  ;; => ({:name "Dave" :age 20 :score 70}
  ;;     {:name "Emma" :age 22 :score 80})

  ;; Example 7: Alternative without threading macro
  (defn parse-numbers-v2 [user-data]
    (update (update user-data :age #(Integer/parseInt %))
            :score #(Integer/parseInt %)))
  ;; Works but harder to read

  ;; Example 8: Demonstrating parseInt behavior
  (Integer/parseInt "42")     ; => 42
  (Integer/parseInt "0")      ; => 0
  (Integer/parseInt "-10")    ; => -10
  ; (Integer/parseInt "abc")  ; => NumberFormatException
)

;; TESTS
;; -----

(defn -test []
  ;; Test standard case
  (assert (= (parse-numbers {:name "John" :age "25" :score "100"})
             {:name "John" :age 25 :score 100})
          "Should parse age and score to integers")

  ;; Test different values
  (assert (= (parse-numbers {:name "Jane" :age "30" :score "95"})
             {:name "Jane" :age 30 :score 95})
          "Should work with different values")

  ;; Test zero score
  (assert (= (parse-numbers {:name "Bob" :age "18" :score "0"})
             {:name "Bob" :age 18 :score 0})
          "Should handle zero correctly")

  ;; Test large numbers
  (assert (= (parse-numbers {:name "Alice" :age "99" :score "9999"})
             {:name "Alice" :age 99 :score 9999})
          "Should handle large numbers")

  ;; Test name unchanged
  (let [result (parse-numbers {:name "Charlie" :age "25" :score "85"})]
    (assert (string? (:name result))
            "Name should remain a string")
    (assert (= (:name result) "Charlie")
            "Name should be unchanged"))

  ;; Test types are correct
  (let [result (parse-numbers {:name "Dave" :age "20" :score "70"})]
    (assert (integer? (:age result))
            "Age should be an integer")
    (assert (integer? (:score result))
            "Score should be an integer"))

  ;; Test mapping over collection
  (assert (= (map parse-numbers [{:name "Emma" :age "22" :score "80"}
                                 {:name "Frank" :age "24" :score "90"}])
             '({:name "Emma" :age 22 :score 80}
               {:name "Frank" :age 24 :score 90}))
          "Should work when mapping over collection")

  (println "✓ All tests passed! The parse-numbers function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
