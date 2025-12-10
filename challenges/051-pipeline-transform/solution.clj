;; =============================================================================
;; 051 - PIPELINE TRANSFORM
;; Level: 11/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates function composition using the threading macro
;; (->) to create a clear, readable transformation pipeline. Each helper
;; function performs one transformation step, and the main function threads
;; the data through all steps in sequence.
;;
;; We use -> (thread-first) rather than comp because it reads left-to-right,
;; matching the natural flow of data transformation. This makes the code more
;; intuitive than comp's right-to-left composition.
;;
;; This pattern is fundamental in functional programming: complex transformations
;; are decomposed into simple, composable steps. Each step is pure, testable,
;; and reusable across different pipelines.

(ns challenge-051.solution
  (:require [clojure.string :as str]))

;; HELPER FUNCTIONS
;; ----------------

(defn trim-strings
  "Trims whitespace from name and email fields.

  Parameters:
  - data: Registration map

  Returns: Map with trimmed strings"
  [data]
  (-> data
      (update :name str/trim)
      (update :email str/trim)))

(defn normalize-email
  "Converts email to lowercase.

  Parameters:
  - data: Registration map

  Returns: Map with normalized email"
  [data]
  (update data :email str/lower-case))

(defn parse-age
  "Converts age from string to integer.

  Parameters:
  - data: Registration map

  Returns: Map with parsed age"
  [data]
  (update data :age #(Integer/parseInt %)))

(defn add-timestamp
  "Adds registered-at timestamp (simulated).

  Parameters:
  - data: Registration map

  Returns: Map with added timestamp"
  [data]
  (assoc data :registered-at "2024-01-15T10:00:00"))

;; MAIN FUNCTION
;; --------------

(defn process-registration
  "Processes registration data through transformation pipeline.

  Pipeline steps:
  1. Trim whitespace from strings
  2. Normalize email to lowercase
  3. Parse age string to integer
  4. Add registration timestamp

  Parameters:
  - raw-data: Untrimmed registration data

  Returns: Cleaned and enriched registration data"
  [raw-data]
  (-> raw-data
      trim-strings
      normalize-email
      parse-age
      add-timestamp))

;; ALTERNATIVE IMPLEMENTATION WITH comp
;; -------------------------------------

(comment
  ;; You can also use comp, but order is reversed (right-to-left)
  (def process-registration-comp
    (comp add-timestamp
          parse-age
          normalize-email
          trim-strings))

  ;; Usage: (process-registration-comp raw-data)
  ;; comp creates a function, -> applies transformations directly
)

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Function Composition with ->
;;    The thread-first macro (->) passes the result of each expression as
;;    the first argument to the next:
;;      (-> data f g h) => (h (g (f data)))
;;    This reads left-to-right, matching natural data flow:
;;      data → trim → normalize → parse → add-timestamp
;;    More intuitive than nested calls or comp.
;;
;; 2. comp vs ->
;;    comp creates a new function by composing functions:
;;      (def pipeline (comp f g h))
;;      (pipeline data)
;;    -> directly applies transformations:
;;      (-> data f g h)
;;    Use comp when you need the composed function as a value.
;;    Use -> when you're applying transformations once.
;;
;; 3. Single-Step Transformations
;;    Each helper performs one transformation:
;;    - trim-strings: only trims
;;    - normalize-email: only lowercases
;;    - parse-age: only parses
;;    - add-timestamp: only adds field
;;    Benefits:
;;    - Each function is independently testable
;;    - Easy to add/remove/reorder steps
;;    - Functions reusable in other pipelines
;;
;; 4. update for Field Transformation
;;    update applies a function to a specific key's value:
;;      (update data :email str/lower-case)
;;    Equivalent to:
;;      (assoc data :email (str/lower-case (:email data)))
;;    But update is more concise and idiomatic.
;;
;; 5. Pure Transformation Pipeline
;;    Each step is pure (no side effects, same input = same output).
;;    The entire pipeline is pure. Benefits:
;;    - Easy to test (deterministic results)
;;    - Easy to reason about (no hidden state)
;;    - Composable (can combine pipelines)
;;    - Parallelizable (in theory, though not needed here)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Transformation pipeline with threading macro
;;
;; Real-world usage: Production code uses threading for data transformation:
;;   (defn process-input [raw-input]
;;     (-> raw-input
;;         clean-whitespace
;;         validate-format
;;         normalize-case
;;         parse-types
;;         enrich-metadata))
;;
;; The reference shows similar patterns where data flows through multiple
;; transformation steps. This is fundamental in functional programming:
;; complex transformations as compositions of simple functions.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Full pipeline with messy input
  (process-registration
    {:name "  Alice Johnson  "
     :email "  ALICE@EXAMPLE.COM  "
     :age "25"})
  ;; => {:name "Alice Johnson"
  ;;     :email "alice@example.com"
  ;;     :age 25
  ;;     :registered-at "2024-01-15T10:00:00"}

  ;; Example 2: Already clean input (still works)
  (process-registration
    {:name "Bob Smith"
     :email "bob@example.com"
     :age "30"})
  ;; => {:name "Bob Smith"
  ;;     :email "bob@example.com"
  ;;     :age 30
  ;;     :registered-at "2024-01-15T10:00:00"}

  ;; Example 3: Mixed case email
  (process-registration
    {:name "Charlie"
     :email "ChArLiE@ExAmPlE.CoM"
     :age "22"})
  ;; => {:email "charlie@example.com"}

  ;; Example 4: Testing individual steps
  (trim-strings {:name "  Test  " :email "  TEST@TEST.COM  "})
  ;; => {:name "Test" :email "TEST@TEST.COM"}

  (normalize-email {:email "TEST@EXAMPLE.COM"})
  ;; => {:email "test@example.com"}

  (parse-age {:age "35"})
  ;; => {:age 35}

  (add-timestamp {})
  ;; => {:registered-at "2024-01-15T10:00:00"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test full pipeline
  (let [result (process-registration
                 {:name "  Alice Johnson  "
                  :email "  ALICE@EXAMPLE.COM  "
                  :age "25"})]
    (assert (= (:name result) "Alice Johnson")
            "Should trim name")
    (assert (= (:email result) "alice@example.com")
            "Should trim and lowercase email")
    (assert (= (:age result) 25)
            "Should parse age to integer")
    (assert (= (:registered-at result) "2024-01-15T10:00:00")
            "Should add timestamp"))

  ;; Test already clean input
  (let [result (process-registration
                 {:name "Bob Smith"
                  :email "BOB@EXAMPLE.COM"
                  :age "30"})]
    (assert (= (:name result) "Bob Smith")
            "Should handle clean name")
    (assert (= (:email result) "bob@example.com")
            "Should still lowercase email"))

  ;; Test individual steps
  (assert (= (trim-strings {:name "  Test  " :email "  test@test.com  "})
             {:name "Test" :email "test@test.com"})
          "trim-strings should trim both fields")

  (assert (= (normalize-email {:email "TEST@EXAMPLE.COM"})
             {:email "test@example.com"})
          "normalize-email should lowercase")

  (assert (= (parse-age {:age "35"})
             {:age 35})
          "parse-age should convert to integer")

  (assert (= (add-timestamp {:name "Test"})
             {:name "Test" :registered-at "2024-01-15T10:00:00"})
          "add-timestamp should add field")

  (println "✓ All tests passed!"))

;; Run: (-test)
