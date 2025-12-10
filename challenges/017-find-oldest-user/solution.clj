;; =============================================================================
;; 017 - FIND OLDEST USER
;; Level: 4/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution uses apply with max-key to find the user with the maximum age.
;; The max-key function is specifically designed for this use case: finding the
;; maximum element based on a key function.
;;
;; We use (apply max-key :age users) which:
;; 1. Applies max-key to the collection of users
;; 2. Uses :age as the key function to extract comparison values
;; 3. Returns the entire user map with the highest age
;;
;; This is more elegant than manually using reduce with a comparison function,
;; though both approaches work. The max-key approach expresses intent clearly:
;; "give me the maximum element by this key."
;;
;; This pattern appears in production code when finding records with extreme
;; values (latest timestamp, highest priority, largest amount, etc.)

(ns challenge-017.solution)

;; IMPLEMENTATION
;; --------------

(defn find-oldest
  "Finds the user with the highest age from a collection.

  Uses max-key to find the user map with the maximum :age value.

  Parameters:
  - users: Non-empty collection of user maps with :name and :age keys

  Returns: User map with the highest age"
  [users]
  ;; Apply max-key to find user with maximum age
  ;; max-key takes a key function (:age) and compares all users by it
  (apply max-key :age users))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. max-key Function
;;    max-key finds the maximum element by applying a key function to each
;;    element and comparing the results.
;;
;;    Signature: (max-key key-fn x y & more)
;;
;;    Examples:
;;    (max-key :age {:name "John" :age 25} {:name "Jane" :age 30})
;;    ;; => {:name "Jane" :age 30}
;;
;;    (max-key count "a" "abc" "ab")
;;    ;; => "abc" (longest string)
;;
;;    (max-key :priority {:task "A" :priority 1}
;;                       {:task "B" :priority 5})
;;    ;; => {:task "B" :priority 5}
;;
;;    Key points:
;;    - Requires at least 2 elements (x and y)
;;    - Returns the original element, not the key value
;;    - If keys are equal, returns first element
;;
;; 2. apply Function
;;    apply invokes a function with arguments from a collection.
;;    It "unpacks" a collection into individual arguments.
;;
;;    Signature: (apply f args* coll)
;;
;;    Examples:
;;    (apply + [1 2 3])             ; => 6 (same as (+ 1 2 3))
;;    (apply max [5 2 9 1])         ; => 9 (same as (max 5 2 9 1))
;;    (apply str ["a" "b" "c"])     ; => "abc"
;;
;;    Why needed for max-key:
;;    ; Without apply - WRONG (passing collection as single arg):
;;    (max-key :age [{:age 25} {:age 30}])  ; Error!
;;
;;    ; With apply - CORRECT (unpacking collection to args):
;;    (apply max-key :age [{:age 25} {:age 30}])  ; Works!
;;
;; 3. Alternative with reduce
;;    You can also solve this with reduce for more control:
;;
;;    (defn find-oldest [users]
;;      (reduce (fn [oldest current]
;;                (if (> (:age current) (:age oldest))
;;                  current
;;                  oldest))
;;              users))
;;
;;    Comparison:
;;    - max-key: More concise, expresses intent clearly
;;    - reduce: More flexible, shows step-by-step logic
;;
;;    Both are valid; max-key is more idiomatic for this case.
;;
;; 4. Keywords as Functions
;;    In Clojure, keywords can be used as functions to look up values:
;;
;;    (:age {:name "John" :age 25})  ; => 25
;;
;;    This is why we can pass :age directly to max-key:
;;    (max-key :age user1 user2)
;;    ; :age acts as (fn [user] (:age user))
;;
;;    Other examples:
;;    (map :name users)           ; Extract all names
;;    (filter :active? users)     ; Filter by :active? flag
;;    (sort-by :priority tasks)   ; Sort by :priority

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo2.md
;;
;; Pattern used: Finding maximum element by key function
;;
;; Real-world usage: Similar patterns appear in production for:
;; - Finding latest record: (apply max-key :timestamp records)
;; - Finding highest priority: (apply max-key :priority tasks)
;; - Finding largest transaction: (apply max-key :amount transactions)
;;
;; The reference code shows aggregation patterns like this used in
;; data processing pipelines where extremes (min/max) need to be found
;; while preserving the full record.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Three users with different ages
  (find-oldest [{:name "John" :age 25}
                {:name "Jane" :age 30}
                {:name "Bob" :age 20}])
  ;; => {:name "Jane" :age 30}

  ;; Example 2: Two users
  (find-oldest [{:name "Alice" :age 45}
                {:name "Charlie" :age 50}])
  ;; => {:name "Charlie" :age 50}

  ;; Example 3: Single user
  (find-oldest [{:name "Solo" :age 99}])
  ;; => {:name "Solo" :age 99}

  ;; Example 4: Tie - multiple users with same max age
  (find-oldest [{:name "Eve" :age 35}
                {:name "Frank" :age 35}
                {:name "Grace" :age 30}])
  ;; => {:name "Eve" :age 35} (returns first with max age)

  ;; Example 5: Users with extra fields preserved
  (find-oldest [{:name "Dave" :age 40 :email "dave@test.com"}
                {:name "Emma" :age 45 :email "emma@test.com"}])
  ;; => {:name "Emma" :age 45 :email "emma@test.com"}

  ;; Example 6: Using result in further operations
  (let [oldest (find-oldest [{:name "John" :age 25}
                             {:name "Jane" :age 30}])]
    (str (:name oldest) " is the oldest at " (:age oldest)))
  ;; => "Jane is the oldest at 30"

  ;; Example 7: Related function - find youngest
  (defn find-youngest [users]
    (apply min-key :age users))
  (find-youngest [{:name "John" :age 25}
                  {:name "Jane" :age 30}
                  {:name "Bob" :age 20}])
  ;; => {:name "Bob" :age 20}
)

;; TESTS
;; -----

(defn -test []
  ;; Test three users
  (assert (= (find-oldest [{:name "John" :age 25}
                           {:name "Jane" :age 30}
                           {:name "Bob" :age 20}])
             {:name "Jane" :age 30})
          "Should return Jane with age 30")

  ;; Test two users
  (assert (= (find-oldest [{:name "Alice" :age 45}
                           {:name "Charlie" :age 50}])
             {:name "Charlie" :age 50})
          "Should return Charlie with age 50")

  ;; Test single user
  (assert (= (find-oldest [{:name "Solo" :age 99}])
             {:name "Solo" :age 99})
          "Should return the only user")

  ;; Test tie (same max age)
  (let [result (find-oldest [{:name "Eve" :age 35}
                             {:name "Frank" :age 35}
                             {:name "Grace" :age 30}])]
    (assert (= (:age result) 35)
            "Should return user with age 35")
    (assert (contains? #{"Eve" "Frank"} (:name result))
            "Should return either Eve or Frank"))

  ;; Test extra fields preserved
  (assert (= (find-oldest [{:name "Dave" :age 40 :email "dave@test.com"}
                           {:name "Emma" :age 45 :email "emma@test.com"}])
             {:name "Emma" :age 45 :email "emma@test.com"})
          "Should preserve extra fields")

  ;; Test large age value
  (assert (= (find-oldest [{:name "Young" :age 18}
                           {:name "Old" :age 100}
                           {:name "Middle" :age 50}])
             {:name "Old" :age 100})
          "Should handle large age values")

  ;; Test all same age
  (let [result (find-oldest [{:name "A" :age 25}
                             {:name "B" :age 25}
                             {:name "C" :age 25}])]
    (assert (= (:age result) 25)
            "Should return user with age 25 when all same"))

  (println "✓ All tests passed! The find-oldest function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
