;; =============================================================================
;; 026 - CALCULATE AVERAGE AGE
;; Level: 6/18 | Type: Pure Function
;; =============================================================================

(ns challenge-026.solution)

(defn average-age
  "Calculates the average age of users. Returns 0 if collection is empty."
  [users]
  (if (empty? users)
    0.0
    (let [total (reduce (fn [sum user] (+ sum (:age user))) 0 users)
          cnt (count users)]
      (double (/ total cnt)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. Avoid division by zero: Check for empty collection first
;; 2. reduce sums all ages with accumulator starting at 0
;; 3. Division (/) in Clojure returns decimal for accurate average

;; REFERENCE PATTERN
;; -----------------
;; Pattern used: Aggregation with reduce and mathematical operations

;; TESTS
;; -----

(defn -test []
  (assert (= (average-age [{:name "John" :age 20}
                           {:name "Jane" :age 30}
                           {:name "Bob" :age 40}])
             30.0))
  (assert (= (average-age [{:name "Alice" :age 25}
                           {:name "Charlie" :age 35}])
             30.0))
  (assert (= (average-age []) 0.0))
  (assert (= (average-age [{:name "Solo" :age 50}]) 50.0))
  (println "✓ All tests passed!"))

;; Run: (-test)
