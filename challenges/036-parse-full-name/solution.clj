;; =============================================================================
;; 036 - PARSE FULL NAME
;; Level: 8/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function parses full names into components (first, middle, last) by
;; splitting on whitespace and handling various edge cases: single names,
;; two-part names, and names with multiple middle parts.
;;
;; The approach uses cond to handle three cases: one word (mononym), two words
;; (no middle name), and three or more words (with middle name). For the multi-
;; word middle name case, we extract the middle parts and join them.
;;
;; This pattern is common in adapters that parse user input and need to handle
;; real-world variability in data formats, where strict assumptions fail.

(ns challenge-036.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn parse-full-name
  "Parses a full name string into first, middle, and last name components.

  Parameters:
  - full-name: Complete name string

  Returns: Map with :first-name, :last-name, and optionally :middle-name"
  [full-name]
  ;; Split by whitespace into parts
  (let [parts (str/split full-name #"\s+")
        part-count (count parts)]
    (cond
      ;; Case 1: Single word (mononym like "Madonna")
      (= part-count 1)
      {:first-name (first parts)
       :last-name (first parts)}

      ;; Case 2: Two words (no middle name)
      (= part-count 2)
      {:first-name (first parts)
       :last-name (last parts)}

      ;; Case 3: Three or more words (has middle name)
      :else
      (let [first-name (first parts)
            last-name (last parts)
            ;; Get everything between first and last
            middle-parts (-> parts rest butlast)
            middle-name (str/join " " middle-parts)]
        {:first-name first-name
         :middle-name middle-name
         :last-name last-name}))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Edge Case Handling with cond
;;    Real-world data is messy: users might enter single names (Madonna, Cher),
;;    two-part names (Jane Doe), or complex names (John Paul George Ringo Starr).
;;    Using cond allows us to handle each case explicitly and correctly.
;;
;; 2. Collection Slicing: rest and butlast
;;    rest returns all elements except the first: [1 2 3 4] → [2 3 4]
;;    butlast returns all elements except the last: [1 2 3 4] → [1 2 3]
;;    Combining them: (butlast (rest [1 2 3 4])) → [2 3]
;;    This extracts middle elements from a collection.
;;
;; 3. Threading for Clarity
;;    (-> parts rest butlast) is more readable than (butlast (rest parts)).
;;    Threading shows the transformation pipeline clearly: start with parts,
;;    remove first element, then remove last element.
;;
;; 4. Conditional Map Keys
;;    We only include :middle-name in the result when it exists (3+ words).
;;    For 1-2 word names, the map doesn't have a :middle-name key at all.
;;    This is better than including :middle-name with nil or "" value.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: String parsing with multiple edge cases
;;
;; Real-world usage: The reference code shows similar pattern for parsing:
;;   (str/split query-string #"&")
;;   (str/split client-assertion #"\.")
;;
;; These demonstrate how production code must handle various input formats,
;; splitting strings and extracting meaningful parts. The reference code also
;; shows defensive programming: checking for empty results, handling missing
;; parts, and validating structure before processing.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Three-part name with single middle name
  (parse-full-name "John Michael Doe")
  ;; => {:first-name "John", :middle-name "Michael", :last-name "Doe"}

  ;; Example 2: Two-part name (no middle)
  (parse-full-name "Jane Doe")
  ;; => {:first-name "Jane", :last-name "Doe"}

  ;; Example 3: Mononym (single name)
  (parse-full-name "Madonna")
  ;; => {:first-name "Madonna", :last-name "Madonna"}

  ;; Example 4: Multiple middle names
  (parse-full-name "John Paul George Ringo Starr")
  ;; => {:first-name "John", :middle-name "Paul George Ringo", :last-name "Starr"}

  ;; Example 5: Simple first and last
  (parse-full-name "Alice Smith")
  ;; => {:first-name "Alice", :last-name "Smith"}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (parse-full-name "John Michael Doe")
             {:first-name "John" :middle-name "Michael" :last-name "Doe"})
          "Should parse three-part name")
  (assert (= (parse-full-name "Jane Doe")
             {:first-name "Jane" :last-name "Doe"})
          "Should parse two-part name without middle")
  (assert (= (parse-full-name "Madonna")
             {:first-name "Madonna" :last-name "Madonna"})
          "Should handle mononym")
  (assert (= (parse-full-name "John Paul George Ringo Starr")
             {:first-name "John" :middle-name "Paul George Ringo" :last-name "Starr"})
          "Should handle multiple middle names")
  ;; Verify no :middle-name key for two-part names
  (let [result (parse-full-name "Jane Doe")]
    (assert (not (contains? result :middle-name))
            "Two-part name should not have :middle-name key"))
  (println "✓ All tests passed!"))

;; Run: (-test)
