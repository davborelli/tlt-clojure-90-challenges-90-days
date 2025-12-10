;; =============================================================================
;; 056 - COMPOSE PREDICATES
;; Level: 12/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates predicate composition using higher-order functions.
;; Instead of writing complex boolean expressions with nested and/or, we compose
;; small, focused predicates that each check one condition.
;;
;; We implement every-pred? (all must pass) and some-pred? (at least one must
;; pass) as generic composition functions. These take a collection of predicates
;; and apply them to a value, combining results with boolean logic.
;;
;; This pattern makes business rules more testable (test each predicate
;; independently), more reusable (predicates work across different contexts),
;; and more expressive (code reads like natural language: "user must be adult
;; and verified and active").

(ns challenge-056.solution)

;; COMPOSITION HELPERS
;; -------------------

(defn every-pred?
  "Returns true if ALL predicates return true for the value.

  Parameters:
  - predicates: Vector of predicate functions
  - value: Value to test

  Returns: Boolean - true if all predicates pass"
  [predicates value]
  (every? (fn [pred] (pred value)) predicates))

(defn some-pred?
  "Returns true if ANY predicate returns true for the value.

  Parameters:
  - predicates: Vector of predicate functions
  - value: Value to test

  Returns: Boolean - true if at least one predicate passes"
  [predicates value]
  (boolean (some (fn [pred] (pred value)) predicates)))

;; INDIVIDUAL PREDICATES
;; ---------------------

(defn adult?
  "Checks if user is 18 or older.

  Parameters:
  - user: User map with :age

  Returns: Boolean"
  [user]
  (>= (:age user) 18))

(defn verified?
  "Checks if user is verified.

  Parameters:
  - user: User map with :verified

  Returns: Boolean"
  [user]
  (true? (:verified user)))

(defn active?
  "Checks if user account is active.

  Parameters:
  - user: User map with :account-status

  Returns: Boolean"
  [user]
  (= (:account-status user) :active))

(defn onboarding-complete?
  "Checks if user completed onboarding.

  Parameters:
  - user: User map with :onboarding-complete

  Returns: Boolean"
  [user]
  (true? (:onboarding-complete user)))

;; MAIN FUNCTION
;; -------------

(defn eligible-user?
  "Checks if user meets all eligibility criteria.

  Criteria (all must be true):
  - Must be adult (18+)
  - Must be verified
  - Must have active account
  - Must have completed onboarding

  Parameters:
  - user: User map

  Returns: Boolean - true if all criteria met"
  [user]
  (every-pred? [adult? verified? active? onboarding-complete?] user))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Predicate Composition
;;    Instead of:
;;      (and (>= (:age user) 18)
;;           (:verified user)
;;           (= (:account-status user) :active)
;;           (:onboarding-complete user))
;;    We compose predicates:
;;      (every-pred? [adult? verified? active? onboarding-complete?] user)
;;    Benefits:
;;    - Each predicate is independently testable
;;    - Predicates are reusable in different contexts
;;    - Code is more expressive and readable
;;
;; 2. Higher-Order Functions for Composition
;;    every-pred? and some-pred? are higher-order functions: they take
;;    functions (predicates) as parameters and combine them.
;;    This is core to functional programming: treating functions as values
;;    that can be passed around, combined, and transformed.
;;
;; 3. every? for Universal Quantification
;;    every? checks if a predicate holds for all items in a collection:
;;      (every? even? [2 4 6]) => true
;;      (every? even? [2 3 6]) => false
;;    We use it to check if all predicates return true for a value.
;;
;; 4. some for Existential Quantification
;;    some checks if any item satisfies a predicate:
;;      (some even? [1 3 5]) => nil (none even)
;;      (some even? [1 2 3]) => true (found even)
;;    We wrap in `boolean` to ensure true/false (some returns truthy/nil).
;;
;; 5. Declarative Business Rules
;;    The eligibility check reads like natural language:
;;      [adult? verified? active? onboarding-complete?]
;;    This is more declarative than imperative boolean expressions.
;;    Easy to add/remove criteria by modifying the vector.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo4.md
;;
;; Pattern used: Predicate composition with higher-order functions
;;
;; Real-world usage: Production code composes predicates for business rules:
;;   (defn eligible-for-loan? [applicant]
;;     (every-pred? [has-income? has-good-credit? below-debt-limit?]
;;                  applicant))
;;
;;   (defn needs-review? [transaction]
;;     (some-pred? [high-amount? foreign-merchant? unusual-pattern?]
;;                 transaction))
;;
;; The reference shows similar composition patterns where complex conditions
;; are built from simple, focused predicates.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Eligible user (all criteria met)
  (eligible-user?
    {:age 25
     :verified true
     :account-status :active
     :onboarding-complete true})
  ;; => true

  ;; Example 2: Not verified
  (eligible-user?
    {:age 25
     :verified false
     :account-status :active
     :onboarding-complete true})
  ;; => false

  ;; Example 3: Underage
  (eligible-user?
    {:age 16
     :verified true
     :account-status :active
     :onboarding-complete true})
  ;; => false

  ;; Example 4: Suspended account
  (eligible-user?
    {:age 25
     :verified true
     :account-status :suspended
     :onboarding-complete true})
  ;; => false

  ;; Example 5: Onboarding not complete
  (eligible-user?
    {:age 25
     :verified true
     :account-status :active
     :onboarding-complete false})
  ;; => false

  ;; Example 6: Testing individual predicates
  (adult? {:age 18})  ;; => true
  (adult? {:age 17})  ;; => false

  (verified? {:verified true})   ;; => true
  (verified? {:verified false})  ;; => false

  ;; Example 7: Testing composition helpers
  (every-pred? [adult? verified?]
               {:age 25 :verified true})  ;; => true

  (some-pred? [adult? verified?]
              {:age 16 :verified false})  ;; => false
)

;; TESTS
;; -----

(defn -test []
  ;; Test eligible user (all criteria met)
  (assert (true? (eligible-user?
                   {:age 25
                    :verified true
                    :account-status :active
                    :onboarding-complete true}))
          "Should return true when all criteria met")

  ;; Test not verified
  (assert (false? (eligible-user?
                    {:age 25
                     :verified false
                     :account-status :active
                     :onboarding-complete true}))
          "Should return false when not verified")

  ;; Test underage
  (assert (false? (eligible-user?
                    {:age 16
                     :verified true
                     :account-status :active
                     :onboarding-complete true}))
          "Should return false when underage")

  ;; Test suspended account
  (assert (false? (eligible-user?
                    {:age 25
                     :verified true
                     :account-status :suspended
                     :onboarding-complete true}))
          "Should return false when account suspended")

  ;; Test onboarding not complete
  (assert (false? (eligible-user?
                    {:age 25
                     :verified true
                     :account-status :active
                     :onboarding-complete false}))
          "Should return false when onboarding not complete")

  ;; Test individual predicates
  (assert (true? (adult? {:age 18}))
          "adult? should return true for 18+")
  (assert (false? (adult? {:age 17}))
          "adult? should return false for <18")

  (assert (true? (verified? {:verified true}))
          "verified? should return true when verified")
  (assert (false? (verified? {:verified false}))
          "verified? should return false when not verified")

  (assert (true? (active? {:account-status :active}))
          "active? should return true for active status")
  (assert (false? (active? {:account-status :suspended}))
          "active? should return false for non-active status")

  ;; Test composition helpers
  (assert (true? (every-pred? [adult? verified?]
                              {:age 25 :verified true}))
          "every-pred? should return true when all pass")
  (assert (false? (every-pred? [adult? verified?]
                               {:age 16 :verified true}))
          "every-pred? should return false when any fails")

  (assert (true? (some-pred? [adult? verified?]
                             {:age 25 :verified false}))
          "some-pred? should return true when at least one passes")
  (assert (false? (some-pred? [adult? verified?]
                              {:age 16 :verified false}))
          "some-pred? should return false when all fail")

  (println "✓ All tests passed!"))

;; Run: (-test)
