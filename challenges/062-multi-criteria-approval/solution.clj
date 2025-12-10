;; =============================================================================
;; 062 - MULTI-CRITERIA APPROVAL
;; Level: 13/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a loan approval decision tree with multiple
;; business rules evaluated in priority order. We first check rejection
;; conditions (fail-fast), then approval conditions, and default to manual
;; review for ambiguous cases.
;;
;; The structure follows the fail-fast principle: quickly identify and reject
;; clearly ineligible applications before spending effort on detailed evaluation.
;; This mirrors production lending systems where clear rejections save processing
;; time and manual review costs.
;;
;; Using cond makes the decision logic explicit and maintainable. Each branch
;; corresponds to a documented business rule that can be audited and updated
;; independently.

(ns challenge-062.solution)

;; IMPLEMENTATION
;; --------------

(defn evaluate-loan-application
  "Evaluates loan application against multiple criteria to determine approval status.

  Decision priority:
  1. Auto-rejection (poor credit, unemployed, excessive debt/loan)
  2. Auto-approval (excellent credit + income + low debt)
  3. Manual review (ambiguous cases)

  Parameters:
  - loan-application: Map with :credit-score, :annual-income, :debt-to-income-ratio,
                      :employment-status, :loan-amount

  Returns: Keyword - :approved, :rejected, or :manual-review"
  [loan-application]
  (let [{:keys [credit-score annual-income debt-to-income-ratio
                employment-status loan-amount]} loan-application]
    (cond
      ;; Auto-rejection conditions (check first - fail fast)
      (< credit-score 600)
      :rejected

      (= employment-status :unemployed)
      :rejected

      (>= debt-to-income-ratio 50)
      :rejected

      (> loan-amount (* annual-income 5))
      :rejected

      ;; Auto-approval conditions (excellent candidates)
      (and (>= credit-score 750)
           (>= annual-income 50000)
           (< debt-to-income-ratio 30)
           (= employment-status :employed))
      :approved

      (and (>= credit-score 800)
           (>= annual-income 75000)
           (< debt-to-income-ratio 40))
      :approved

      ;; Manual review (default for ambiguous cases)
      :else
      :manual-review)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Fail-Fast Principle
;;    Check rejection conditions first to quickly eliminate ineligible applicants:
;;    - Saves processing time (don't evaluate complex rules for clear rejections)
;;    - Reduces load on manual review teams
;;    - Provides faster feedback to applicants
;;    This is a key pattern in high-volume decision systems.
;;
;; 2. Multi-Factor Decision Making
;;    Real-world approvals rarely depend on a single factor. This function
;;    combines:
;;    - Credit score (historical payment behavior)
;;    - Income (repayment capacity)
;;    - Debt-to-income ratio (current financial obligations)
;;    - Employment status (income stability)
;;    - Loan amount (risk magnitude)
;;    Each factor provides a different signal about risk.
;;
;; 3. Defensive Defaults
;;    Using :manual-review as the default is defensive programming:
;;    - Ambiguous cases get human review (safer than auto-approval)
;;    - Prevents edge cases from slipping through
;;    - Allows system to evolve (new rules can be added without changing default)
;;    Better to err on the side of caution in financial systems.
;;
;; 4. Priority-Based Rule Evaluation
;;    Order matters! If we checked approvals before rejections:
;;      Credit: 800, Income: 100K, Debt: 60%, Employed
;;    Would approve (high credit + income) despite 60% debt ratio.
;;    Checking rejections first catches this: 60% debt → reject.
;;
;; 5. Business Rule Transparency
;;    Each cond branch is a documented business rule:
;;    - Easy to audit compliance
;;    - Simple to modify when regulations change
;;    - Clear for non-technical stakeholders to review
;;    This makes the system maintainable and auditable.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Multi-branch cond for complex approval logic
;;
;; Real-world usage: The reference shows similar approval flows:
;;   (cond
;;     (disqualifying-factor?) :rejected
;;     (auto-approve-eligible?) :approved
;;     :else :manual-review)
;;
;; Production lending systems use this pattern for:
;; - Credit card applications
;; - Mortgage pre-approvals
;; - Personal loans
;; - Business credit lines
;;
;; The pattern scales: start with simple rules, add complexity over time
;; based on data and regulatory requirements.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Auto-approved - excellent credit, good income, low debt
  (evaluate-loan-application
    {:credit-score 780
     :annual-income 60000
     :debt-to-income-ratio 25
     :employment-status :employed
     :loan-amount 150000})
  ;; => :approved

  ;; Example 2: Auto-rejected - poor credit score
  (evaluate-loan-application
    {:credit-score 550
     :annual-income 40000
     :debt-to-income-ratio 35
     :employment-status :employed
     :loan-amount 100000})
  ;; => :rejected

  ;; Example 3: Manual review - borderline case
  (evaluate-loan-application
    {:credit-score 680
     :annual-income 55000
     :debt-to-income-ratio 32
     :employment-status :self-employed
     :loan-amount 120000})
  ;; => :manual-review

  ;; Example 4: Auto-approved - very high credit and income
  (evaluate-loan-application
    {:credit-score 820
     :annual-income 80000
     :debt-to-income-ratio 38
     :employment-status :self-employed
     :loan-amount 200000})
  ;; => :approved

  ;; Example 5: Auto-rejected - unemployed
  (evaluate-loan-application
    {:credit-score 750
     :annual-income 0
     :debt-to-income-ratio 0
     :employment-status :unemployed
     :loan-amount 50000})
  ;; => :rejected

  ;; Example 6: Auto-rejected - excessive debt ratio
  (evaluate-loan-application
    {:credit-score 720
     :annual-income 60000
     :debt-to-income-ratio 55
     :employment-status :employed
     :loan-amount 100000})
  ;; => :rejected

  ;; Example 7: Auto-rejected - loan too large for income
  (evaluate-loan-application
    {:credit-score 700
     :annual-income 40000
     :debt-to-income-ratio 30
     :employment-status :employed
     :loan-amount 250000})  ;; > 40000 * 5 = 200000
  ;; => :rejected
)

;; TESTS
;; -----

(defn -test []
  ;; Test auto-approval cases
  (assert (= (evaluate-loan-application
               {:credit-score 780 :annual-income 60000 :debt-to-income-ratio 25
                :employment-status :employed :loan-amount 150000})
             :approved)
          "Should auto-approve: excellent credit, good income, low debt")

  (assert (= (evaluate-loan-application
               {:credit-score 820 :annual-income 80000 :debt-to-income-ratio 38
                :employment-status :self-employed :loan-amount 200000})
             :approved)
          "Should auto-approve: very high credit and income")

  ;; Test auto-rejection cases
  (assert (= (evaluate-loan-application
               {:credit-score 550 :annual-income 40000 :debt-to-income-ratio 35
                :employment-status :employed :loan-amount 100000})
             :rejected)
          "Should auto-reject: poor credit score")

  (assert (= (evaluate-loan-application
               {:credit-score 750 :annual-income 0 :debt-to-income-ratio 0
                :employment-status :unemployed :loan-amount 50000})
             :rejected)
          "Should auto-reject: unemployed")

  (assert (= (evaluate-loan-application
               {:credit-score 720 :annual-income 60000 :debt-to-income-ratio 55
                :employment-status :employed :loan-amount 100000})
             :rejected)
          "Should auto-reject: excessive debt ratio")

  (assert (= (evaluate-loan-application
               {:credit-score 700 :annual-income 40000 :debt-to-income-ratio 30
                :employment-status :employed :loan-amount 250000})
             :rejected)
          "Should auto-reject: loan too large for income")

  ;; Test manual review cases
  (assert (= (evaluate-loan-application
               {:credit-score 680 :annual-income 55000 :debt-to-income-ratio 32
                :employment-status :self-employed :loan-amount 120000})
             :manual-review)
          "Should require manual review: borderline case")

  (println "✓ All tests passed! The evaluate-loan-application function works correctly."))

;; Run: (-test)
