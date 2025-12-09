;; =============================================================================
;; 008 - ADD STATUS FIELD
;; Level: 2/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter demonstrates how to enrich data by adding new fields to maps.
;; This is a common pattern when:
;; - Adding default values to records
;; - Adding computed or derived fields
;; - Adding metadata (timestamps, status, etc.)
;; - Preparing data for storage or transmission
;;
;; We use assoc, which is the idiomatic way to add or update keys in a map.
;; It returns a new map with the added key-value pair, leaving the original
;; map unchanged (immutability).

(ns challenge-008.solution)

;; IMPLEMENTATION
;; --------------

(defn add-status
  "Adds a status field with value :active to a user map.

  This function enriches user data by adding a :status field set to :active,
  which might represent that the user account is currently active in the system.

  Parameters:
  - user: Map with user information

  Returns: New map with :status :active added"
  [user]
  ;; Use assoc to add the :status field with value :active
  (assoc user :status :active))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. assoc Function
;;    assoc (associate) adds or updates key-value pairs in a map.
;;
;;    Syntax: (assoc map key value)
;;           (assoc map k1 v1 k2 v2 ...)
;;
;;    Examples:
;;    (assoc {} :a 1)
;;    ;; => {:a 1}
;;
;;    (assoc {:a 1} :b 2)
;;    ;; => {:a 1 :b 2}
;;
;;    (assoc {:a 1} :a 100)
;;    ;; => {:a 100}  ; Updates existing key
;;
;;    Multiple keys at once:
;;    (assoc {} :a 1 :b 2 :c 3)
;;    ;; => {:a 1 :b 2 :c 3}
;;
;; 2. Immutability in Clojure
;;    assoc does NOT modify the original map. It returns a new map.
;;
;;    (def original {:name "John"})
;;    (def updated (assoc original :age 30))
;;    ;; original is still {:name "John"}
;;    ;; updated is {:name "John" :age 30}
;;
;;    Benefits:
;;    - Safe for concurrent programming
;;    - No unexpected side effects
;;    - Easy to reason about
;;    - Enables time travel debugging
;;
;;    Clojure uses structural sharing internally, so copying is efficient.
;;
;; 3. Keywords as Values
;;    Keywords are often used for enumerated values like status:
;;    - :active, :inactive, :pending
;;    - :success, :error, :processing
;;    - :low, :medium, :high
;;
;;    Benefits over strings:
;;    - Interned (same keyword object is reused)
;;    - Faster equality checks
;;    - Can be used as functions: (:name user)
;;    - Conventional in Clojure code
;;
;; 4. Data Enrichment Pattern
;;    Common scenarios for adding fields:
;;
;;    a) Default values:
;;       (assoc user :role :member)
;;
;;    b) Timestamps:
;;       (assoc user :created-at (java.time.Instant/now))
;;
;;    c) Computed fields:
;;       (assoc user :full-name (str (:first-name user) " " (:last-name user)))
;;
;;    d) Multiple fields:
;;       (assoc user
;;              :status :active
;;              :role :member
;;              :created-at (java.time.Instant/now))

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo4.md
;;
;; Pattern used: Data enrichment by adding fields
;;
;; Real-world usage: Adding fields to maps is fundamental in:
;; - API request/response handling (add metadata)
;; - Database operations (add timestamps, IDs)
;; - Event processing (add correlation IDs)
;; - Data pipelines (add processing stage markers)
;;
;; The reference code shows similar patterns where adapters transform
;; data between representations, often adding or removing fields in the process.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Add status to user
  (add-status {:name "John" :email "john@example.com"})
  ;; => {:name "John" :email "john@example.com" :status :active}

  ;; Example 2: Different user
  (add-status {:name "Jane" :email "jane@example.com"})
  ;; => {:name "Jane" :email "jane@example.com" :status :active}

  ;; Example 3: Another user
  (add-status {:name "Bob" :email "bob@test.com"})
  ;; => {:name "Bob" :email "bob@test.com" :status :active}

  ;; Example 4: User with extra fields
  (add-status {:name "Alice"
               :email "alice@example.com"
               :age 30
               :city "New York"})
  ;; => {:name "Alice" :email "alice@example.com" :age 30 :city "New York" :status :active}

  ;; Example 5: Demonstrating immutability
  (def original-user {:name "Charlie" :email "charlie@example.com"})
  (def updated-user (add-status original-user))
  ;; original-user is unchanged: {:name "Charlie" :email "charlie@example.com"}
  ;; updated-user has status: {:name "Charlie" :email "charlie@example.com" :status :active}

  ;; Example 6: If :status already exists, it would be overwritten
  (add-status {:name "Dave" :email "dave@example.com" :status :inactive})
  ;; => {:name "Dave" :email "dave@example.com" :status :active}
  ;; Note: Original :inactive status is replaced with :active

  ;; Example 7: Empty name/email
  (add-status {:name "" :email ""})
  ;; => {:name "" :email "" :status :active}

  ;; Example 8: Using in a pipeline
  (->> {:name "Eve" :email "eve@example.com"}
       (add-status)
       (assoc :role :admin))
  ;; => {:name "Eve" :email "eve@example.com" :status :active :role :admin}
)

;; TESTS
;; -----

(defn -test []
  (assert (= (add-status {:name "John" :email "john@example.com"})
             {:name "John" :email "john@example.com" :status :active})
          "Should add :status :active to user map")

  (assert (= (add-status {:name "Jane" :email "jane@example.com"})
             {:name "Jane" :email "jane@example.com" :status :active})
          "Should work for different users")

  (assert (= (add-status {:name "Bob" :email "bob@test.com"})
             {:name "Bob" :email "bob@test.com" :status :active})
          "Should preserve all existing fields")

  (assert (= (add-status {:name "Alice"
                          :email "alice@example.com"
                          :age 30
                          :city "New York"})
             {:name "Alice"
              :email "alice@example.com"
              :age 30
              :city "New York"
              :status :active})
          "Should work with maps containing extra fields")

  ;; Test immutability
  (let [original {:name "Charlie" :email "charlie@example.com"}
        updated (add-status original)]
    (assert (= original {:name "Charlie" :email "charlie@example.com"})
            "Should not modify original map")
    (assert (= updated {:name "Charlie" :email "charlie@example.com" :status :active})
            "Should return new map with status"))

  (assert (= (add-status {:name "Dave" :email "dave@example.com" :status :inactive})
             {:name "Dave" :email "dave@example.com" :status :active})
          "Should overwrite existing :status field")

  (assert (= (add-status {:name "" :email ""})
             {:name "" :email "" :status :active})
          "Should work with empty string values")

  (println "✓ All tests passed! The add-status function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
