;; =============================================================================
;; 001 - CHECK IF ADULT
;; Level: 1/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This is a simple predicate function that compares age with the legal
;; threshold of 18. Since comparison operators in Clojure return boolean
;; values directly, we can simply return the result of the >= operation.
;;
;; This approach is preferred over using an if statement because it's more
;; concise and idiomatic. The >= operator already returns exactly what we need
;; (true or false), so there's no reason for additional conditional logic.
;;
;; This pattern is common in production code for simple boolean checks,
;; as seen in reference examples where risk ratings and validations
;; are performed using direct comparison operations.

(ns challenge-001.solution)

;; IMPLEMENTATION
;; --------------

(defn adult?
  "Checks if a person is of legal age (18 years or older).

  Parameters:
  - age: The person's age in years (integer)

  Returns: Boolean - true if age >= 18, false otherwise"
  [age]
  ;; Compare age with legal threshold of 18
  ;; Returns true if age >= 18, false otherwise
  (>= age 18))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Predicates in Clojure
;;    By convention, predicate functions (those returning booleans)
;;    end with a question mark (?). This naming makes code more
;;    readable by clearly indicating boolean returns.
;;    Examples: empty?, nil?, even?, odd?, contains?, adult?
;;
;; 2. Comparison Operators
;;    Clojure has standard comparison operators for numbers:
;;    - =  (equal)
;;    - <  (less than)
;;    - >  (greater than)
;;    - <= (less than or equal)
;;    - >= (greater than or equal)
;;    - not= (not equal)
;;    All return boolean values (true or false).
;;
;; 3. Pure Functions
;;    This function is pure because it satisfies three requirements:
;;    a) Same input always produces same output
;;       - (adult? 18) will ALWAYS return true
;;       - (adult? 17) will ALWAYS return false
;;    b) No side effects - doesn't modify anything outside scope
;;       - No printing, no I/O, no global state changes
;;    c) Doesn't depend on external state
;;       - Only uses input parameter
;;
;;    Pure functions are easier to test, reason about, and parallelize.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo4.md
;;
;; Pattern used: Simple boolean predicate with comparison operator
;;
;; Real-world usage: The reference code uses this pattern to check if
;; risk rating allows automated processing:
;;   (and (= risk-rating :low) (= risk-reason :fast-analysis-queue))
;;
;; This shows how simple predicates are building blocks for complex
;; business logic in production systems.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Exactly legal age
  (adult? 18)
  ;; => true

  ;; Example 2: One year below legal age
  (adult? 17)
  ;; => false

  ;; Example 3: Well above legal age
  (adult? 25)
  ;; => true

  ;; Example 4: Edge case - newborn
  (adult? 0)
  ;; => false

  ;; Example 5: Edge case - very old
  (adult? 100)
  ;; => true

  ;; Example 6: Teenage years
  (adult? 16)
  ;; => false
)

;; TESTS
;; -----

(defn -test []
  (assert (= (adult? 18) true)
          "Should return true for age 18 (exactly legal)")
  (assert (= (adult? 17) false)
          "Should return false for age 17 (below legal)")
  (assert (= (adult? 25) true)
          "Should return true for age 25 (above legal)")
  (assert (= (adult? 0) false)
          "Edge case - should return false for age 0")
  (assert (= (adult? 100) true)
          "Edge case - should return true for age 100")
  (assert (= (adult? 16) false)
          "Should return false for teenager age 16")
  (assert (= (adult? 19) true)
          "Should return true for age 19 (just above)")
  (println "✓ All tests passed! The adult? function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
