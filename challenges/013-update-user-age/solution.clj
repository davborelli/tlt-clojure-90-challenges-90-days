;; =============================================================================
;; 013 - UPDATE USER AGE
;; Level: 3/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates value transformation using the update function.
;; Unlike assoc which replaces a value with a new one, update transforms the
;; existing value by applying a function to it.
;;
;; This pattern is essential when:
;; - Incrementing/decrementing counters
;; - Applying calculations to numeric values
;; - Transforming strings (uppercase, trim, etc.)
;; - Modifying nested collections
;;
;; We use update with inc (increment) to add 1 to the age. This is more
;; expressive than extracting, incrementing, and re-inserting the value manually.

(ns challenge-013.solution)

;; IMPLEMENTATION
;; --------------

(defn birthday
  "Increments a user's age by 1, simulating a birthday.

  Transforms the user map by incrementing the :age value.

  Parameters:
  - user: Map with :name and :age keys

  Returns: New map with :age incremented by 1"
  [user]
  ;; Use update to increment the :age field
  (update user :age inc))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. update Function
;;    update transforms a value in a map by applying a function to it.
;;
;;    Syntax: (update map key f)
;;           (update map key f arg1 arg2 ...)
;;
;;    Examples:
;;    (update {:a 1} :a inc)
;;    ;; => {:a 2}
;;
;;    (update {:name "john"} :name str/upper-case)
;;    ;; => {:name "JOHN"}
;;
;;    (update {:count 5} :count + 10)
;;    ;; => {:count 15}
;;
;;    (update {:items [1 2]} :items conj 3)
;;    ;; => {:items [1 2 3]}
;;
;;    If key doesn't exist, f is called with nil:
;;    (update {} :count (fnil inc 0))
;;    ;; => {:count 1}
;;
;; 2. update vs assoc
;;    Understand when to use each:
;;
;;    assoc: Replace value with new value
;;    (assoc {:a 1} :a 2)
;;    ;; => {:a 2}
;;
;;    update: Transform existing value
;;    (update {:a 1} :a inc)
;;    ;; => {:a 2}
;;
;;    update is better when:
;;    - New value depends on old value
;;    - Applying a function/calculation
;;    - More expressive of intent
;;
;;    assoc is better when:
;;    - Setting to a constant value
;;    - Value doesn't depend on old value
;;    - Simpler and more direct
;;
;; 3. inc and dec Functions
;;    Built-in functions for incrementing/decrementing numbers:
;;
;;    inc: Adds 1
;;    (inc 5)   ;; => 6
;;    (inc 0)   ;; => 1
;;    (inc -1)  ;; => 0
;;
;;    dec: Subtracts 1
;;    (dec 5)   ;; => 4
;;    (dec 1)   ;; => 0
;;    (dec 0)   ;; => -1
;;
;;    These are more idiomatic than (+ x 1) or (- x 1).
;;
;; 4. Common update Patterns
;;    update is versatile for many transformations:
;;
;;    a) Numeric operations:
;;       (update user :age inc)
;;       (update cart :total + item-price)
;;       (update stats :count dec)
;;
;;    b) String operations:
;;       (update person :name str/upper-case)
;;       (update data :email str/trim)
;;
;;    c) Collection operations:
;;       (update user :tags conj :verified)
;;       (update state :items rest)
;;
;;    d) Nested updates:
;;       (update-in user [:address :zip] str/upper-case)
;;
;;    e) With default value using fnil:
;;       (update counter :count (fnil inc 0))
;;       ; If :count is nil, use 0, then increment
;;
;; 5. Functional Updates Pattern
;;    update enables clean functional transformations:
;;
;;    Manual approach (verbose):
;;    (let [old-age (:age user)
;;          new-age (inc old-age)]
;;      (assoc user :age new-age))
;;
;;    With update (concise):
;;    (update user :age inc)
;;
;;    Chaining updates:
;;    (-> user
;;        (update :age inc)
;;        (update :login-count inc)
;;        (assoc :last-login (now)))

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Value transformation with update
;;
;; Real-world usage: update appears constantly for:
;; - Counters (page views, attempts, retries)
;; - Accumulation (totals, balances)
;; - Modifications (price adjustments, discounts)
;; - State transitions (status updates)
;;
;; The reference code shows adapters transforming data, and update is a
;; fundamental tool for these transformations when the new value depends
;; on the current value.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Young adult
  (birthday {:name "John" :age 25})
  ;; => {:name "John" :age 26}

  ;; Example 2: Turning 18 (legal age)
  (birthday {:name "Jane" :age 17})
  ;; => {:name "Jane" :age 18}

  ;; Example 3: First birthday
  (birthday {:name "Bob" :age 0})
  ;; => {:name "Bob" :age 1}

  ;; Example 4: Becoming centenarian
  (birthday {:name "Alice" :age 99})
  ;; => {:name "Alice" :age 100}

  ;; Example 5: With extra fields preserved
  (birthday {:name "Charlie"
             :age 30
             :email "charlie@example.com"
             :city "New York"})
  ;; => {:name "Charlie" :age 31 :email "charlie@example.com" :city "New York"}

  ;; Example 6: Multiple birthdays (successive updates)
  (-> {:name "Dave" :age 20}
      (birthday)
      (birthday)
      (birthday))
  ;; => {:name "Dave" :age 23}

  ;; Example 7: Using with map for multiple users
  (map birthday [{:name "Eve" :age 25}
                 {:name "Frank" :age 30}
                 {:name "Grace" :age 35}])
  ;; => ({:name "Eve" :age 26}
  ;;     {:name "Frank" :age 31}
  ;;     {:name "Grace" :age 36})

  ;; Example 8: Demonstrating immutability
  (def original {:name "Henry" :age 40})
  (def updated (birthday original))
  ;; original is still {:name "Henry" :age 40}
  ;; updated is {:name "Henry" :age 41}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (birthday {:name "John" :age 25})
             {:name "John" :age 26})
          "Should increment age by 1")

  (assert (= (birthday {:name "Jane" :age 17})
             {:name "Jane" :age 18})
          "Should work when turning 18")

  (assert (= (birthday {:name "Bob" :age 0})
             {:name "Bob" :age 1})
          "Should work for first birthday")

  (assert (= (birthday {:name "Alice" :age 99})
             {:name "Alice" :age 100})
          "Should work for large ages")

  (assert (= (birthday {:name "Charlie"
                        :age 30
                        :email "charlie@example.com"
                        :city "New York"})
             {:name "Charlie"
              :age 31
              :email "charlie@example.com"
              :city "New York"})
          "Should preserve extra fields")

  ;; Test multiple successive updates
  (assert (= (-> {:name "Dave" :age 20}
                 (birthday)
                 (birthday)
                 (birthday))
             {:name "Dave" :age 23})
          "Should work with successive updates")

  ;; Test immutability
  (let [original {:name "Henry" :age 40}
        updated (birthday original)]
    (assert (= original {:name "Henry" :age 40})
            "Should not modify original map")
    (assert (= updated {:name "Henry" :age 41})
            "Should return new map with incremented age"))

  ;; Test with map
  (assert (= (map birthday [{:name "Eve" :age 25}
                            {:name "Frank" :age 30}])
             '({:name "Eve" :age 26}
               {:name "Frank" :age 31}))
          "Should work when mapping over collection")

  (println "✓ All tests passed! The birthday function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
