;; =============================================================================
;; 011 - SUM AGES
;; Level: 3/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function demonstrates collection aggregation using reduce, one of the
;; most powerful higher-order functions in functional programming. While map
;; transforms each element and filter selects elements, reduce combines all
;; elements into a single value.
;;
;; We use reduce to accumulate the sum by extracting each user's age and adding
;; it to the running total. Starting with an initial value of 0 ensures the
;; function handles empty collections correctly.
;;
;; This pattern appears constantly when computing totals, averages, maximums,
;; or any other aggregate value from a collection.

(ns challenge-011.solution)

;; IMPLEMENTATION
;; --------------

(defn sum-ages
  "Calculates the total sum of ages from a collection of users.

  Takes a vector of user maps and returns the sum of all their ages.
  Returns 0 for an empty collection.

  Parameters:
  - users: Vector of maps with :name and :age keys

  Returns: Integer representing the sum of all ages"
  [users]
  ;; Use reduce to accumulate the sum of ages
  (reduce (fn [acc user]
            (+ acc (:age user)))
          0
          users))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. reduce Function
;;    reduce is a higher-order function that combines collection elements
;;    into a single value by repeatedly applying a reducing function.
;;
;;    Syntax: (reduce f init coll)
;;           (reduce f coll)  ; uses first element as init
;;
;;    The reducing function f takes two arguments:
;;    - Accumulator (the result so far)
;;    - Current element from the collection
;;
;;    Examples:
;;    (reduce + 0 [1 2 3 4])
;;    ;; => 10
;;    ;; Steps: 0+1=1, 1+2=3, 3+3=6, 6+4=10
;;
;;    (reduce * 1 [2 3 4])
;;    ;; => 24
;;    ;; Steps: 1*2=2, 2*3=6, 6*4=24
;;
;;    (reduce conj [] [1 2 3])
;;    ;; => [1 2 3]
;;
;; 2. reduce with Initial Value
;;    Always provide an initial value for predictable behavior:
;;
;;    With initial value:
;;    (reduce + 0 [])      ;; => 0
;;    (reduce + 0 [5])     ;; => 5
;;    (reduce + 0 [1 2 3]) ;; => 6
;;
;;    Without initial value:
;;    (reduce + [])        ;; Error! No initial value for empty coll
;;    (reduce + [5])       ;; => 5 (returns single element)
;;    (reduce + [1 2 3])   ;; => 6 (uses 1 as initial value)
;;
;;    Best practice: Always provide initial value for robustness.
;;
;; 3. Reducing Function Patterns
;;    The reducing function can be:
;;
;;    a) Anonymous function:
;;       (reduce (fn [acc x] (+ acc x)) 0 coll)
;;
;;    b) Built-in function:
;;       (reduce + 0 coll)
;;
;;    c) Short anonymous function:
;;       (reduce #(+ %1 %2) 0 coll)
;;
;;    d) Custom named function:
;;       (defn add-age [total user]
;;         (+ total (:age user)))
;;       (reduce add-age 0 users)
;;
;; 4. Common reduce Use Cases
;;    reduce is versatile for many aggregation patterns:
;;
;;    Sum:
;;    (reduce + 0 numbers)
;;
;;    Count:
;;    (reduce (fn [count _] (inc count)) 0 coll)
;;
;;    Max:
;;    (reduce max numbers)
;;
;;    Build map:
;;    (reduce (fn [m [k v]] (assoc m k v))
;;            {}
;;            [[:a 1] [:b 2]])
;;    ;; => {:a 1 :b 2}
;;
;;    Group by:
;;    (reduce (fn [m user]
;;              (update m (:role user) (fnil conj []) user))
;;            {}
;;            users)
;;
;; 5. Alternative Approaches
;;    For this specific problem, we could:
;;
;;    a) map then reduce (two-pass):
;;       (->> users
;;            (map :age)
;;            (reduce + 0))
;;
;;    b) reduce with inline extraction (one-pass, this solution):
;;       (reduce (fn [acc user] (+ acc (:age user))) 0 users)
;;
;;    c) Using transduce (one-pass, no intermediate collection):
;;       (transduce (map :age) + 0 users)
;;
;;    The inline reduce is a good balance of clarity and efficiency.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Collection aggregation with reduce
;;
;; Real-world usage: reduce is used everywhere for:
;; - Financial calculations (sum transactions, compute balance)
;; - Analytics (count events, compute averages)
;; - Data transformation (build maps, group items)
;; - Validation (all items valid?, any item invalid?)
;;
;; reduce is one of the fundamental building blocks of functional programming,
;; alongside map and filter. Together they form the "trinity" of collection
;; operations.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Multiple users
  (sum-ages [{:name "John" :age 25}
             {:name "Jane" :age 30}
             {:name "Bob" :age 45}])
  ;; => 100

  ;; Example 2: Single user
  (sum-ages [{:name "Alice" :age 18}])
  ;; => 18

  ;; Example 3: Empty collection
  (sum-ages [])
  ;; => 0

  ;; Example 4: Different ages
  (sum-ages [{:name "Diana" :age 20}
             {:name "Eve" :age 22}
             {:name "Frank" :age 28}])
  ;; => 70

  ;; Example 5: Many users
  (sum-ages [{:name "User1" :age 10}
             {:name "User2" :age 20}
             {:name "User3" :age 30}
             {:name "User4" :age 40}])
  ;; => 100

  ;; Example 6: All same age
  (sum-ages [{:name "A" :age 25}
             {:name "B" :age 25}
             {:name "C" :age 25}])
  ;; => 75

  ;; Example 7: Large ages
  (sum-ages [{:name "Senior1" :age 80}
             {:name "Senior2" :age 85}])
  ;; => 165

  ;; Example 8: Using in combination with filter
  ;; Sum ages of only adults
  (->> [{:name "Child" :age 10}
        {:name "Adult1" :age 25}
        {:name "Adult2" :age 30}]
       (filter #(>= (:age %) 18))
       (sum-ages))
  ;; => 55
)

;; TESTS
;; -----

(defn -test []
  (assert (= (sum-ages [{:name "John" :age 25}
                        {:name "Jane" :age 30}
                        {:name "Bob" :age 45}])
             100)
          "Should sum multiple ages correctly")

  (assert (= (sum-ages [{:name "Alice" :age 18}])
             18)
          "Should work with single user")

  (assert (= (sum-ages [])
             0)
          "Should return 0 for empty collection")

  (assert (= (sum-ages [{:name "Diana" :age 20}
                        {:name "Eve" :age 22}
                        {:name "Frank" :age 28}])
             70)
          "Should sum different ages")

  (assert (= (sum-ages [{:name "User1" :age 10}
                        {:name "User2" :age 20}
                        {:name "User3" :age 30}
                        {:name "User4" :age 40}])
             100)
          "Should work with many users")

  (assert (= (sum-ages [{:name "A" :age 25}
                        {:name "B" :age 25}
                        {:name "C" :age 25}])
             75)
          "Should work when all ages are the same")

  (assert (= (sum-ages [{:name "Senior1" :age 80}
                        {:name "Senior2" :age 85}])
             165)
          "Should work with large ages")

  ;; Test with filtering
  (assert (= (->> [{:name "Child" :age 10}
                   {:name "Adult1" :age 25}
                   {:name "Adult2" :age 30}]
                  (filter #(>= (:age %) 18))
                  (sum-ages))
             55)
          "Should work when combined with filter")

  (println "✓ All tests passed! The sum-ages function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
