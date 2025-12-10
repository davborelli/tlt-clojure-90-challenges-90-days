;; =============================================================================
;; 027 - CHECK IF ALL USERS ARE ADULTS
;; Level: 6/18 | Type: Pure Function
;; =============================================================================

(ns challenge-027.solution)

(defn all-adults?
  "Checks if all users are adults (age >= 18).

  Parameters:
  - users: Collection of user maps with :age

  Returns: Boolean - true if all are 18+, false otherwise"
  [users]
  (every? #(>= (:age %) 18) users))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. every? tests if predicate is true for ALL items
;; 2. Returns true for empty collection (vacuous truth)
;; 3. Short-circuits on first false (efficient)
;; 4. Opposite of some? (which checks if ANY match)

;; REFERENCE PATTERN
;; -----------------
;; Pattern used: Universal quantification with every?

;; TESTS
;; -----

(defn -test []
  (assert (= (all-adults? [{:name "John" :age 25}
                           {:name "Jane" :age 30}])
             true))
  (assert (= (all-adults? [{:name "John" :age 25}
                           {:name "Bob" :age 17}])
             false))
  (assert (= (all-adults? []) true))
  (assert (= (all-adults? [{:name "Alice" :age 18}]) true))
  (assert (= (all-adults? [{:name "Child" :age 10}]) false))
  (println "✓ All tests passed!"))

;; Run: (-test)
