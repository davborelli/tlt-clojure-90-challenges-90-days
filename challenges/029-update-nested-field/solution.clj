;; =============================================================================
;; 029 - UPDATE NESTED FIELD
;; Level: 6/18 | Type: Adapter
;; =============================================================================

(ns challenge-029.solution)

(defn increment-age
  "Increments the age field in a nested profile structure.

  Parameters:
  - profile: Nested map with age at [:user :details :age]

  Returns: Map with age incremented by 1"
  [profile]
  (update-in profile [:user :details :age] inc))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. update-in: Updates value at nested path
;; 2. Path vector navigates through nested maps
;; 3. Function (inc) applied to value at path
;; 4. Returns new map with all structure preserved

;; REFERENCE PATTERN
;; -----------------
;; Pattern used: Nested map transformation with update-in

;; TESTS
;; -----

(defn -test []
  (assert (= (increment-age {:user {:name "John" :details {:age 25}}})
             {:user {:name "John" :details {:age 26}}}))
  (assert (= (increment-age {:user {:name "Jane" :details {:age 30}}})
             {:user {:name "Jane" :details {:age 31}}}))
  ;; Test name preserved
  (let [result (increment-age {:user {:name "Bob" :details {:age 40}}})]
    (assert (= (get-in result [:user :name]) "Bob")))
  (println "✓ All tests passed!"))

;; Run: (-test)
