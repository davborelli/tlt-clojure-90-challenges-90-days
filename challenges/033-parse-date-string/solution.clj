;; =============================================================================
;; 033 - PARSE DATE STRING
;; Level: 7/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms date strings from external systems (wire format)
;; into structured maps (domain format) suitable for business logic processing.
;; We split the ISO date string and convert each component to an integer.
;;
;; The approach uses destructuring to extract year, month, and day in one step,
;; then applies type coercion (string to integer) to each component. This is
;; a fundamental pattern in adapters that transform external data formats.
;;
;; This pattern is common in production adapters that receive date strings from
;; APIs and need to convert them into structured data for validation or storage.

(ns challenge-033.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn parse-date
  "Parses ISO date string (YYYY-MM-DD) into a structured map.

  Parameters:
  - date-string: Date in format \"YYYY-MM-DD\"

  Returns: Map with :year, :month, :day as integers"
  [date-string]
  ;; Split by hyphen and destructure into year, month, day strings
  (let [[year-str month-str day-str] (str/split date-string #"-")]
    ;; Convert each string to integer and build result map
    {:year  (Integer/parseInt year-str)
     :month (Integer/parseInt month-str)
     :day   (Integer/parseInt day-str)}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Type Coercion with Integer/parseInt
;;    Java interop allows us to call Integer/parseInt to convert strings to ints.
;;    This is the standard way to parse integer strings in Clojure.
;;    Example: (Integer/parseInt "2024") => 2024
;;    Note: This throws an exception for invalid input like "abc".
;;
;; 2. Destructuring in let
;;    [year-str month-str day-str] destructures the result of split.
;;    This binds the first element to year-str, second to month-str, etc.
;;    It's more readable than using (first ...), (second ...), (nth ... 2).
;;
;; 3. Map Literal Construction
;;    {:year ... :month ... :day ...} is map literal syntax.
;;    The order of keys doesn't matter in Clojure maps.
;;    This is the most common way to construct maps with known keys.
;;
;; 4. Adapter Pattern: Wire → Domain
;;    This is a classic adapter pattern transforming external format (string)
;;    to internal format (structured map with typed fields).
;;    Wire format: "2024-01-15" (external system format)
;;    Domain format: {:year 2024 :month 1 :day 15} (internal format)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Type coercion in adapters (wire → domain transformation)
;;
;; Real-world usage: The reference code shows similar patterns where
;; external data is parsed and converted to typed domain models:
;;   (entry->record db-entry) - converts database strings to domain types
;;   (record->entry domain-record) - converts domain types back to strings
;;
;; This bidirectional transformation is essential in adapters that bridge
;; external systems (databases, APIs) with internal domain logic.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Start of year
  (parse-date "2024-01-15")
  ;; => {:year 2024, :month 1, :day 15}

  ;; Example 2: End of year
  (parse-date "1999-12-31")
  ;; => {:year 1999, :month 12, :day 31}

  ;; Example 3: Mid-year date
  (parse-date "2000-06-01")
  ;; => {:year 2000, :month 6, :day 1}

  ;; Example 4: Leap year
  (parse-date "2024-02-29")
  ;; => {:year 2024, :month 2, :day 29}

  ;; Example 5: Single digit month and day have leading zeros
  (parse-date "2023-03-05")
  ;; => {:year 2023, :month 3, :day 5}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (parse-date "2024-01-15")
             {:year 2024 :month 1 :day 15})
          "Should parse date with single-digit month")
  (assert (= (parse-date "1999-12-31")
             {:year 1999 :month 12 :day 31})
          "Should parse end of year date")
  (assert (= (parse-date "2000-06-01")
             {:year 2000 :month 6 :day 1})
          "Should parse date with single-digit day")
  (assert (= (parse-date "2024-02-29")
             {:year 2024 :month 2 :day 29})
          "Should parse leap year date")
  (assert (= (parse-date "2023-03-05")
             {:year 2023 :month 3 :day 5})
          "Should handle leading zeros in month and day")
  (println "✓ All tests passed!"))

;; Run: (-test)
