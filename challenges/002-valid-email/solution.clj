;; =============================================================================
;; 002 - VALID EMAIL
;; Level: 1/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function performs a basic email validation by checking if the input
;; string contains an @ symbol. While this isn't comprehensive email validation,
;; it's a practical first-pass check used in many applications.
;;
;; We use clojure.string/includes? which is a simple and efficient way to check
;; for substring presence. This is better than regex for such a simple check.

(ns challenge-002.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn valid-email?
  "Checks if a string contains @ symbol (basic email validation).

  Parameters:
  - email: String to validate

  Returns: Boolean - true if contains @, false otherwise"
  [email]
  ;; Check if the email string contains @ character
  (and (not (str/blank? email))
       (str/includes? email "@")))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. String Functions in Clojure
;;    The clojure.string namespace provides utilities for string manipulation.
;;    - str/includes? checks if string contains a substring
;;    - str/blank? checks if string is nil, empty, or only whitespace
;;    - Always require: (:require [clojure.string :as str])
;;
;; 2. Combining Predicates with and
;;    The and operator evaluates expressions left-to-right and returns
;;    false as soon as any expression is false. This is called short-circuit
;;    evaluation and is efficient for chaining multiple checks.
;;
;; 3. Guard Clauses
;;    Checking str/blank? first prevents errors with nil or empty strings.
;;    This is a common defensive programming pattern in production code.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo4.md
;;
;; Pattern used: Simple predicate with boolean logic (and operator)
;;
;; Real-world usage: Production code often uses similar patterns to validate
;; input data before processing, combining multiple simple checks.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid email format
  (valid-email? "user@example.com")
  ;; => true

  ;; Example 2: Missing @ symbol
  (valid-email? "invalid-email")
  ;; => false

  ;; Example 3: Empty string
  (valid-email? "")
  ;; => false

  ;; Example 4: Just @ symbol
  (valid-email? "@")
  ;; => true

  ;; Example 5: Multiple @ symbols (still returns true)
  (valid-email? "user@@example.com")
  ;; => true
)

;; TESTS
;; -----

(defn -test []
  (assert (= (valid-email? "user@example.com") true)
          "Valid email should return true")
  (assert (= (valid-email? "invalid-email") false)
          "Email without @ should return false")
  (assert (= (valid-email? "") false)
          "Empty string should return false")
  (assert (= (valid-email? "@") true)
          "Just @ should return true")
  (assert (= (valid-email? "test@test") true)
          "Simple email should return true")
  (assert (= (valid-email? nil) false)
          "Nil should return false")
  (println "✓ All tests passed! The valid-email? function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
