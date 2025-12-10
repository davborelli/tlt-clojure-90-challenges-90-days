;; =============================================================================
;; 016 - COUNT VALID EMAILS
;; Level: 4/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates function composition by combining filter and count.
;; Rather than manually iterating through the collection, we use Clojure's
;; composable functions to express the logic declaratively: "filter the valid
;; emails, then count them."
;;
;; This approach is more maintainable than using a single reduce operation with
;; a counter because it separates concerns: validation logic is isolated in the
;; predicate function, and counting is handled by count. This makes the code
;; easier to test, understand, and modify.
;;
;; The pattern of composing filter + count appears frequently in data processing
;; pipelines where you need statistics about subsets of data.

(ns challenge-016.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn valid-email?
  "Helper predicate that checks if an email is valid.

  An email is valid if:
  - It is not blank (empty or only whitespace)
  - It contains the @ symbol

  Parameters:
  - email: String to validate

  Returns: Boolean - true if valid, false otherwise"
  [email]
  (and (not (str/blank? email))
       (str/includes? email "@")))

(defn count-valid-emails
  "Counts how many valid email addresses exist in a collection.

  Filters the collection for valid emails (containing @ and not blank),
  then counts the results.

  Parameters:
  - emails: Collection of email strings

  Returns: Integer - count of valid emails"
  [emails]
  ;; Filter for valid emails, then count the results
  (count (filter valid-email? emails)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Function Composition Pattern
;;    Instead of writing complex logic in a single function, we compose
;;    smaller functions to build the desired behavior. This approach:
;;    - Makes code more readable (clear intent)
;;    - Enables reuse (valid-email? can be used elsewhere)
;;    - Simplifies testing (test each function independently)
;;    - Follows Unix philosophy: do one thing well
;;
;;    Example compositions:
;;    (count (filter pred coll))     ; Count matching items
;;    (apply + (map f coll))         ; Sum transformed values
;;    (first (sort coll))            ; Find minimum
;;
;; 2. filter Function
;;    filter takes a predicate and a collection, returning a lazy sequence
;;    of items for which the predicate returns true.
;;
;;    Signature: (filter pred coll)
;;
;;    Examples:
;;    (filter even? [1 2 3 4])      ; => (2 4)
;;    (filter pos? [-1 0 1 2])      ; => (1 2)
;;    (filter #(> % 5) [1 6 3 8])   ; => (6 8)
;;
;;    Key points:
;;    - Returns lazy sequence (not realized until needed)
;;    - Original collection unchanged (immutable)
;;    - Predicate should be side-effect free
;;    - Returns empty sequence if no matches
;;
;; 3. count Function
;;    count returns the number of items in a collection. Works with any
;;    collection type: vectors, lists, sets, maps, lazy sequences.
;;
;;    Examples:
;;    (count [1 2 3])              ; => 3
;;    (count #{:a :b})             ; => 2
;;    (count {:a 1 :b 2})          ; => 2 (counts key-value pairs)
;;    (count (filter even? [1 2 3])) ; => 1
;;
;;    Performance note:
;;    - O(1) for vectors, maps, sets (counted collections)
;;    - O(n) for lazy sequences (must realize to count)
;;
;; 4. Helper Functions for Clarity
;;    Extracting valid-email? as a separate function has benefits:
;;    - Naming makes intent clear ("is this email valid?")
;;    - Can be tested independently
;;    - Can be reused in other functions
;;    - Logic is in one place (DRY principle)
;;
;;    Compare:
;;    ; Without helper (less clear):
;;    (count (filter #(and (not (str/blank? %))
;;                         (str/includes? % "@"))
;;                   emails))
;;
;;    ; With helper (more clear):
;;    (count (filter valid-email? emails))

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Composing filter + count for data quality metrics
;;
;; Real-world usage: The reference code shows similar composition patterns
;; for cleaning and validating data:
;;   (remove-spaces-and-empty coll) => (->> coll (remove str/blank?) ...)
;;
;; In production systems, this pattern appears in:
;; - Data quality reports (count valid vs invalid records)
;; - User analytics (count active users, verified emails, etc.)
;; - Validation pipelines (how many items passed validation?)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Mixed valid and invalid emails
  (count-valid-emails ["john@example.com" "invalid" "jane@test.com"])
  ;; => 2

  ;; Example 2: Including blank entries
  (count-valid-emails ["test@test.com" "" "   " "user@domain.org"])
  ;; => 2

  ;; Example 3: All invalid
  (count-valid-emails ["invalid" "no-at-sign" ""])
  ;; => 0

  ;; Example 4: Empty collection
  (count-valid-emails [])
  ;; => 0

  ;; Example 5: All valid
  (count-valid-emails ["a@b.com" "test@example.org" "user@domain.net"])
  ;; => 3

  ;; Example 6: Edge case - only @ symbol
  (count-valid-emails ["@" "test@test.com"])
  ;; => 2 (@ alone is technically valid by our simple rule)

  ;; Example 7: Using with other operations
  (let [total 10
        valid (count-valid-emails ["a@b.com" "invalid" "c@d.com"])
        invalid (- total valid)]
    {:valid valid :invalid invalid})
  ;; => {:valid 2 :invalid 8}

  ;; Example 8: Demonstrating helper function reuse
  (filter valid-email? ["john@example.com" "invalid" "jane@test.com"])
  ;; => ("john@example.com" "jane@test.com")
)

;; TESTS
;; -----

(defn -test []
  ;; Test mixed valid and invalid
  (assert (= (count-valid-emails ["john@example.com" "invalid" "jane@test.com"])
             2)
          "Should count 2 valid emails out of 3")

  ;; Test with blank entries
  (assert (= (count-valid-emails ["test@test.com" "" "   " "user@domain.org"])
             2)
          "Should ignore blank entries")

  ;; Test all invalid
  (assert (= (count-valid-emails ["invalid" "no-at-sign" ""])
             0)
          "Should return 0 for all invalid")

  ;; Test empty collection
  (assert (= (count-valid-emails [])
             0)
          "Should return 0 for empty collection")

  ;; Test all valid
  (assert (= (count-valid-emails ["a@b.com" "test@example.org" "user@domain.net"])
             3)
          "Should count all 3 valid emails")

  ;; Test edge case - @ symbol alone
  (assert (= (count-valid-emails ["@" "test@test.com"])
             2)
          "@ alone should count as valid by our simple rule")

  ;; Test single valid email
  (assert (= (count-valid-emails ["single@test.com"])
             1)
          "Should count single valid email")

  ;; Test helper function independently
  (assert (= (valid-email? "test@example.com") true)
          "Helper should validate correct email")
  (assert (= (valid-email? "invalid") false)
          "Helper should reject email without @")
  (assert (= (valid-email? "") false)
          "Helper should reject blank email")

  (println "✓ All tests passed! The count-valid-emails function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
