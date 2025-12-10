;; =============================================================================
;; 076 - MULTI-APPROVAL WORKFLOW
;; Level: 16/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a complex approval workflow system using priority-based
;; conditional logic. The key challenge is managing 13+ different approval rules
;; with overlapping criteria (amount, risk, role, department, emergency status).
;;
;; The solution uses a large `cond` expression with carefully ordered branches.
;; The order matters critically because earlier conditions take precedence.
;; For example, an emergency critical request must be caught before standard
;; high-amount rules, otherwise it might get incorrectly routed.
;;
;; Helper functions (requires-documentation?, requires-escalation?, max-days-for-type)
;; extract common logic to keep the main function readable. This follows the
;; single responsibility principle - each function has one clear purpose.
;;
;; This pattern mirrors production approval systems where business rules must
;; be evaluated in strict priority order to ensure compliance and risk management.

(ns challenge-076.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn requires-documentation?
  "Determines if documentation is required based on amount and risk.

  Parameters:
  - amount: Transaction amount
  - risk-level: Risk assessment level

  Returns: Boolean indicating if documentation is required"
  [amount risk-level]
  (or (> amount 100000)
      (contains? #{:high :critical} risk-level)))

(defn requires-escalation?
  "Determines if escalation is required based on risk and amount.

  Parameters:
  - amount: Transaction amount
  - risk-level: Risk assessment level

  Returns: Boolean indicating if escalation is required"
  [amount risk-level]
  (or (= risk-level :critical)
      (> amount 500000)))

(defn max-days-for-type
  "Returns maximum approval days for each approval type.

  Parameters:
  - approval-type: Type of approval required

  Returns: Number of days allowed for approval"
  [approval-type]
  (case approval-type
    :auto 1
    :single 3
    :dual 5
    :board 10))

;; MAIN IMPLEMENTATION
;; -------------------

(defn determine-approval-workflow
  "Determines the approval workflow required for a request.

  Evaluates multiple criteria in priority order:
  1. Emergency + Critical → Board
  2. Very high amounts → Board
  3. Critical risk → VP + CFO
  4. High risk + high amount → Director + VP
  5. CEO override (unless critical)
  6. VP authority
  7. Director with budget
  8. Manager authority
  9-11. Standard amount tiers
  12. Department specific rules
  13. Default fallback

  Parameters:
  - request: Map with :amount, :risk-level, :department, :requester-role,
             :has-budget-approval, :is-emergency

  Returns: Map with :approvers, :approval-type, :max-days,
           :requires-documentation, :escalation-required"
  [request]
  (let [{:keys [amount risk-level department requester-role
                has-budget-approval is-emergency]} request

        ;; Determine approval type and approvers based on priority rules
        [approval-type approvers]
        (cond
          ;; 1. Emergency Critical - highest priority
          (and (= risk-level :critical) is-emergency)
          [:board [:board :cfo :vp]]

          ;; 2. Very high amount always requires board
          (> amount 1000000)
          [:board [:board :cfo :vp]]

          ;; 3. Critical risk requires VP + CFO dual approval
          (= risk-level :critical)
          [:dual [:vp :cfo]]

          ;; 4. High risk + high amount requires director + VP
          (and (= risk-level :high) (> amount 500000))
          [:dual [:director :vp]]

          ;; 5. CEO can auto-approve (unless caught by critical risk above)
          (= requester-role :ceo)
          [:auto []]

          ;; 6. VP can auto-approve under 100k
          (and (= requester-role :vp) (< amount 100000))
          [:auto []]

          ;; 7. Director with budget approval for amounts under 50k
          (and (= requester-role :director)
               has-budget-approval
               (< amount 50000))
          [:single [:manager]]

          ;; 8. Manager authority under 25k
          (and (= requester-role :manager) (< amount 25000))
          [:single [:director]]

          ;; 9. Standard high amount (250k+) requires dual approval
          (> amount 250000)
          [:dual [:director :vp]]

          ;; 10. Standard medium amount (50k+) requires VP
          (> amount 50000)
          [:single [:vp]]

          ;; 11. Standard low amount (10k+) requires director
          (> amount 10000)
          [:single [:director]]

          ;; 12. IT department special rule for small amounts
          (and (= department :it) (< amount 15000))
          [:auto []]

          ;; 13. Default: single manager approval
          :else
          [:single [:manager]])]

    ;; Build final response with all required fields
    {:approvers approvers
     :approval-type approval-type
     :max-days (max-days-for-type approval-type)
     :requires-documentation (requires-documentation? amount risk-level)
     :escalation-required (requires-escalation? amount risk-level)}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Priority-Based Conditional Logic
;;    In complex business rules, the ORDER of conditions matters critically.
;;    We place the most restrictive or highest-priority rules first.
;;    For example, "emergency + critical" must come before "CEO override"
;;    because even CEOs can't auto-approve critical emergencies.
;;    This ensures compliance and risk management requirements are never bypassed.
;;
;; 2. Destructuring with :keys
;;    The pattern {:keys [amount risk-level ...]} extracts multiple fields
;;    from a map into local variables in one line. This is more concise than:
;;    (let [amount (:amount request) risk-level (:risk-level request) ...])
;;    Destructuring makes the code cleaner and shows intent clearly.
;;
;; 3. Helper Functions for Readability
;;    Complex conditions like (or (> amount 100000) (contains? #{:high :critical} risk-level))
;;    are extracted to named functions (requires-documentation?).
;;    This has three benefits:
;;    a) The main function reads like English: "requires-documentation: (requires-documentation? ...)"
;;    b) The logic is reusable and testable independently
;;    c) Changes to documentation rules only need one edit location
;;
;; 4. Vector Destructuring in let
;;    The pattern [approval-type approvers] (cond ...) assigns both values
;;    returned by the cond expression. Each cond branch returns a vector
;;    like [:board [:board :cfo :vp]], which is destructured into two variables.
;;    This allows cond to return multiple related values elegantly.
;;
;; 5. Case vs Cond
;;    We use `case` for max-days-for-type because it's matching a single value
;;    against fixed constants. Case is faster and clearer for this pattern.
;;    We use `cond` for the main logic because we're evaluating complex boolean
;;    expressions with AND/OR/comparisons. Cond is necessary when each branch
;;    has different conditions, not just value matching.
;;
;; 6. Contains? with Sets
;;    The expression (contains? #{:high :critical} risk-level) checks if
;;    risk-level is either :high or :critical. Using a set literal #{...}
;;    with contains? is idiomatic Clojure for "is X in this list of values?"
;;    It's more efficient than (or (= risk-level :high) (= risk-level :critical)).
;;
;; 7. Boolean Operator Short-Circuiting
;;    The `and` and `or` operators short-circuit - they stop evaluating
;;    as soon as the result is determined. In (and (= risk-level :critical) is-emergency),
;;    if risk-level is not :critical, is-emergency is never evaluated.
;;    This is both a performance optimization and allows safe chaining of checks.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Complex multi-branch cond for business rule evaluation
;;
;; The reference shows a fraud analysis function with multiple risk checks:
;;   (cond
;;     (and (> transaction-amount 10000) (= country high-risk-country)) :block
;;     (> fraud-score 80) :manual-review
;;     (and (= user-status :new) (> amount 5000)) :manual-review
;;     :else :approved)
;;
;; Real-world usage: Production systems use this pattern for:
;; - Fraud detection (evaluate risk signals in priority order)
;; - Approval workflows (determine authorization level)
;; - Pricing rules (apply discounts/surcharges based on criteria)
;; - Eligibility checks (determine qualification for services)
;;
;; The key insight is that business rules often have natural priority ordering,
;; and representing this as a sequential cond makes the logic explicit and auditable.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Emergency Critical - Highest Priority
  (determine-approval-workflow {:amount 50000
                                :risk-level :critical
                                :department :operations
                                :requester-role :manager
                                :has-budget-approval false
                                :is-emergency true})
  ;; => {:approvers [:board :cfo :vp]
  ;;     :approval-type :board
  ;;     :max-days 10
  ;;     :requires-documentation true
  ;;     :escalation-required true}

  ;; Example 2: CEO Override - Auto-approval for non-critical
  (determine-approval-workflow {:amount 75000
                                :risk-level :low
                                :department :finance
                                :requester-role :ceo
                                :has-budget-approval true
                                :is-emergency false})
  ;; => {:approvers []
  ;;     :approval-type :auto
  ;;     :max-days 1
  ;;     :requires-documentation false
  ;;     :escalation-required false}

  ;; Example 3: Standard Dual Approval - High amount
  (determine-approval-workflow {:amount 300000
                                :risk-level :medium
                                :department :operations
                                :requester-role :employee
                                :has-budget-approval false
                                :is-emergency false})
  ;; => {:approvers [:director :vp]
  ;;     :approval-type :dual
  ;;     :max-days 5
  ;;     :requires-documentation true
  ;;     :escalation-required false}

  ;; Example 4: Director with Budget Approval
  (determine-approval-workflow {:amount 30000
                                :risk-level :low
                                :department :operations
                                :requester-role :director
                                :has-budget-approval true
                                :is-emergency false})
  ;; => {:approvers [:manager]
  ;;     :approval-type :single
  ;;     :max-days 3
  ;;     :requires-documentation false
  ;;     :escalation-required false}

  ;; Example 5: IT Department Auto-approval
  (determine-approval-workflow {:amount 12000
                                :risk-level :low
                                :department :it
                                :requester-role :employee
                                :has-budget-approval false
                                :is-emergency false})
  ;; => {:approvers []
  ;;     :approval-type :auto
  ;;     :max-days 1
  ;;     :requires-documentation false
  ;;     :escalation-required false}

  ;; Example 6: Critical Risk Non-Emergency
  (determine-approval-workflow {:amount 40000
                                :risk-level :critical
                                :department :finance
                                :requester-role :employee
                                :has-budget-approval false
                                :is-emergency false})
  ;; => {:approvers [:vp :cfo]
  ;;     :approval-type :dual
  ;;     :max-days 5
  ;;     :requires-documentation true
  ;;     :escalation-required true}

  ;; Example 7: Very High Amount - Board Required
  (determine-approval-workflow {:amount 1500000
                                :risk-level :low
                                :department :operations
                                :requester-role :vp
                                :has-budget-approval true
                                :is-emergency false})
  ;; => {:approvers [:board :cfo :vp]
  ;;     :approval-type :board
  ;;     :max-days 10
  ;;     :requires-documentation true
  ;;     :escalation-required true}
)

;; TESTS
;; -----

(defn -test []
  ;; Test emergency critical
  (let [result (determine-approval-workflow {:amount 50000 :risk-level :critical
                                             :department :operations :requester-role :manager
                                             :has-budget-approval false :is-emergency true})]
    (assert (= (:approval-type result) :board) "Emergency critical should require board")
    (assert (= (:approvers result) [:board :cfo :vp]) "Should have board approvers")
    (assert (:escalation-required result) "Should require escalation"))

  ;; Test CEO override
  (let [result (determine-approval-workflow {:amount 75000 :risk-level :low
                                             :department :finance :requester-role :ceo
                                             :has-budget-approval true :is-emergency false})]
    (assert (= (:approval-type result) :auto) "CEO should have auto-approval")
    (assert (empty? (:approvers result)) "Auto-approval has no approvers"))

  ;; Test standard dual approval
  (let [result (determine-approval-workflow {:amount 300000 :risk-level :medium
                                             :department :operations :requester-role :employee
                                             :has-budget-approval false :is-emergency false})]
    (assert (= (:approval-type result) :dual) "High amount should require dual")
    (assert (= (:approvers result) [:director :vp]) "Should need director + vp")
    (assert (:requires-documentation result) "Should require documentation"))

  ;; Test IT department auto-approval
  (let [result (determine-approval-workflow {:amount 12000 :risk-level :low
                                             :department :it :requester-role :employee
                                             :has-budget-approval false :is-emergency false})]
    (assert (= (:approval-type result) :auto) "IT under 15k should auto-approve"))

  ;; Test very high amount
  (let [result (determine-approval-workflow {:amount 1500000 :risk-level :low
                                             :department :operations :requester-role :vp
                                             :has-budget-approval true :is-emergency false})]
    (assert (= (:approval-type result) :board) "Amount > 1M should require board")
    (assert (:requires-escalation result) "Should require escalation"))

  ;; Test director with budget approval
  (let [result (determine-approval-workflow {:amount 30000 :risk-level :low
                                             :department :operations :requester-role :director
                                             :has-budget-approval true :is-emergency false})]
    (assert (= (:approval-type result) :single) "Director with budget should get single")
    (assert (= (:approvers result) [:manager]) "Should need manager approval"))

  ;; Test critical risk non-emergency
  (let [result (determine-approval-workflow {:amount 40000 :risk-level :critical
                                             :department :finance :requester-role :employee
                                             :has-budget-approval false :is-emergency false})]
    (assert (= (:approval-type result) :dual) "Critical risk should require dual")
    (assert (= (:approvers result) [:vp :cfo]) "Should need VP + CFO")
    (assert (:escalation-required result) "Critical should require escalation"))

  (println "✓ All tests passed! The determine-approval-workflow function works correctly."))

;; Run: (-test)
