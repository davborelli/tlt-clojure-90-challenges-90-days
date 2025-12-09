;; =============================================================================
;; 006 - FILTER ADULTS
;; Level: 2/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function demonstrates collection filtering using the filter higher-order
;; function. We apply a predicate (a function returning true/false) to each
;; element, and filter keeps only the elements where the predicate returns true.
;;
;; We use an anonymous function to extract the :age from each user map and
;; check if it's >= 18. The filter function automatically handles the iteration
;; and builds a new collection with only the matching elements.
;;
;; This pattern is fundamental in functional programming and appears constantly
;; in production code when working with collections.

(ns challenge-006.solution)

;; IMPLEMENTATION
;; --------------

(defn filter-adults
  "Filters a collection of users, keeping only adults (age >= 18).

  The function processes a collection of user maps and returns a new
  collection containing only those users who are of legal age.

  Parameters:
  - users: Vector of maps with :name and :age keys

  Returns: Vector of user maps where :age >= 18"
  [users]
  ;; Use filter with a predicate that checks age >= 18
  (filterv #(>= (:age %) 18) users))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. filter Function
;;    filter is a higher-order function that takes:
;;    - A predicate function (returns true/false)
;;    - A collection
;;
;;    It returns a new collection with only elements where predicate returns true.
;;
;;    Syntax: (filter pred coll)
;;
;;    Example:
;;    (filter odd? [1 2 3 4 5])
;;    ;; => (1 3 5)
;;
;;    Important: filter returns a lazy sequence. For immediate evaluation,
;;    use filterv (filter + vec) which returns a vector.
;;
;; 2. Anonymous Functions
;;    Clojure has two syntaxes for anonymous functions:
;;
;;    a) Short form with #():
;;       #(>= (:age %) 18)
;;       - % represents the argument
;;       - %1, %2, %3 for multiple arguments
;;       - Concise for simple functions
;;
;;    b) Full form with fn:
;;       (fn [user] (>= (:age user) 18))
;;       - More readable for complex logic
;;       - Can have multiple arity
;;       - Can have docstrings
;;
;;    Choose #() for one-liners, fn for anything more complex.
;;
;; 3. Higher-Order Functions
;;    Functions that take other functions as arguments or return functions.
;;    Core examples:
;;    - filter: keeps elements matching predicate
;;    - map: transforms each element
;;    - reduce: combines elements into single value
;;    - remove: opposite of filter (removes matching elements)
;;
;;    Higher-order functions enable declarative, composable code.
;;
;; 4. filterv vs filter
;;    - filter: Returns lazy sequence (not evaluated until needed)
;;    - filterv: Returns vector (eager evaluation)
;;
;;    Use filterv when:
;;    - You need immediate results
;;    - You'll access elements multiple times
;;    - You want a vector, not a sequence
;;
;;    Use filter when:
;;    - Working with infinite sequences
;;    - Results will be used only once
;;    - Want to defer computation

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Collection filtering with predicate functions
;;
;; Real-world usage: The reference code shows similar patterns:
;; - remove-spaces-and-empty: Uses remove to filter out unwanted elements
;; - Processing lists of items with specific criteria
;;
;; Filtering collections is ubiquitous in:
;; - Data validation (filter valid items)
;; - User permissions (filter allowed resources)
;; - Search results (filter matching criteria)
;; - Data pipelines (filter relevant events)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Mixed ages - some adults, some minors
  (filter-adults [{:name "John" :age 25}
                  {:name "Jane" :age 17}
                  {:name "Bob" :age 30}])
  ;; => [{:name "John" :age 25} {:name "Bob" :age 30}]

  ;; Example 2: All minors - returns empty
  (filter-adults [{:name "Alice" :age 16}
                  {:name "Charlie" :age 15}])
  ;; => []

  ;; Example 3: All adults including exactly 18
  (filter-adults [{:name "Diana" :age 18}
                  {:name "Eve" :age 18}])
  ;; => [{:name "Diana" :age 18} {:name "Eve" :age 18}]

  ;; Example 4: Empty input
  (filter-adults [])
  ;; => []

  ;; Example 5: Single adult
  (filter-adults [{:name "Frank" :age 21}])
  ;; => [{:name "Frank" :age 21}]

  ;; Example 6: Edge case - exactly 18 is included
  (filter-adults [{:name "Grace" :age 18}
                  {:name "Henry" :age 17}])
  ;; => [{:name "Grace" :age 18}]

  ;; Example 7: Large age differences
  (filter-adults [{:name "Ivy" :age 80}
                  {:name "Jack" :age 10}
                  {:name "Kate" :age 45}])
  ;; => [{:name "Ivy" :age 80} {:name "Kate" :age 45}]

  ;; Example 8: Demonstrating order preservation
  (filter-adults [{:name "Z" :age 25}
                  {:name "A" :age 30}
                  {:name "M" :age 17}])
  ;; => [{:name "Z" :age 25} {:name "A" :age 30}]
  ;; Order is preserved: Z comes before A
)

;; TESTS
;; -----

(defn -test []
  (assert (= (filter-adults [{:name "John" :age 25}
                             {:name "Jane" :age 17}
                             {:name "Bob" :age 30}])
             [{:name "John" :age 25}
              {:name "Bob" :age 30}])
          "Should filter out minors and keep adults")

  (assert (= (filter-adults [{:name "Alice" :age 16}
                             {:name "Charlie" :age 15}])
             [])
          "Should return empty vector when all are minors")

  (assert (= (filter-adults [{:name "Diana" :age 18}
                             {:name "Eve" :age 18}])
             [{:name "Diana" :age 18}
              {:name "Eve" :age 18}])
          "Should include users exactly 18 years old")

  (assert (= (filter-adults [])
             [])
          "Should return empty vector for empty input")

  (assert (= (filter-adults [{:name "Frank" :age 21}])
             [{:name "Frank" :age 21}])
          "Should work with single adult")

  (assert (= (filter-adults [{:name "Grace" :age 18}
                             {:name "Henry" :age 17}])
             [{:name "Grace" :age 18}])
          "Should include 18 but exclude 17")

  (assert (= (filter-adults [{:name "Ivy" :age 80}
                             {:name "Jack" :age 10}
                             {:name "Kate" :age 45}])
             [{:name "Ivy" :age 80}
              {:name "Kate" :age 45}])
          "Should work with large age ranges")

  ;; Test order preservation
  (assert (= (filter-adults [{:name "Z" :age 25}
                             {:name "A" :age 30}
                             {:name "M" :age 17}])
             [{:name "Z" :age 25}
              {:name "A" :age 30}])
          "Should preserve original order")

  (println "✓ All tests passed! The filter-adults function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
