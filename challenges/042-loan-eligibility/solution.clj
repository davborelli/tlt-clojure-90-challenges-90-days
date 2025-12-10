;; =============================================================================
;; 042 - DETERMINE LOAN ELIGIBILITY
;; Level: 9/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a loan eligibility decision system that evaluates
;; multiple criteria in a specific order. Each rejection criterion is checked
;; sequentially, and we return immediately upon finding a disqualifying factor.
;;
;; The approach uses cond to implement the decision tree, with each branch
;; checking one disqualification criterion. The final else branch handles the
;; approval case when all criteria pass. We return both a boolean decision and
;; a human-readable reason for audit and user communication.
;;
;; This pattern is standard in financial services for automated lending
;; decisions, credit card approvals, and risk assessment, where regulatory
;; requirements demand explainable decisions.

(ns challenge-042.solution)

;; IMPLEMENTATION
;; --------------

(defn check-eligibility
  "Determines loan eligibility based on applicant criteria.

  Parameters:
  - applicant: Map with :age, :income, :credit-score, :employed, :debt

  Returns: Map with :eligible (boolean) and :reason (string)"
  [applicant]
  (let [{:keys [age income credit-score employed debt]} applicant]
    (cond
      ;; Check 1: Age must be between 18 and 70
      (or (< age 18) (> age 70))
      {:eligible false
       :reason "Age outside eligible range"}

      ;; Check 2: Minimum income requirement
      (< income 30000)
      {:eligible false
       :reason "Income below minimum"}

      ;; Check 3: Minimum credit score
      (< credit-score 600)
      {:eligible false
       :reason "Credit score too low"}

      ;; Check 4: Must be employed
      (not employed)
      {:eligible false
       :reason "Employment required"}

      ;; Check 5: Debt-to-income ratio must be <= 40%
      (> debt (* income 0.4))
      {:eligible false
       :reason "Debt-to-income ratio too high"}

      ;; All checks passed - approve
      :else
      {:eligible true
       :reason "Applicant meets all criteria"})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Explainable Decisions
;;    Returning both a boolean decision and a reason string makes the system
;;    transparent. This is crucial in regulated industries (finance, healthcare,
;;    insurance) where decisions must be explainable for:
;;    - Regulatory compliance (Fair Lending laws)
;;    - Customer communication (why was I rejected?)
;;    - Audit trails (why did we approve this loan?)
;;
;; 2. Sequential Evaluation Order
;;    The order of checks matters for user experience and efficiency:
;;    - Check cheapest/fastest criteria first (age, employment)
;;    - Check data-dependent criteria next (income, credit score)
;;    - Check calculated criteria last (debt-to-income ratio)
;;    This fails fast on simple disqualifications before expensive checks.
;;
;; 3. Compound Conditions for Ranges
;;    (or (< age 18) (> age 70)) checks if age is outside a range.
;;    This is clearer than (not (and (>= age 18) (<= age 70))).
;;    For range checks, think "too low OR too high" rather than negating
;;    "within range".
;;
;; 4. Debt-to-Income Ratio Calculation
;;    (* income 0.4) calculates 40% of income.
;;    (> debt (* income 0.4)) checks if debt exceeds this threshold.
;;    This is a standard financial metric: debt should not exceed 40% of income.
;;    Example: $50k income → max $20k debt. $25k debt > $20k → rejected.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md, exemplo5.md
;;
;; Pattern used: Multi-branch cond for eligibility/approval decisions
;;
;; Real-world usage: The reference code shows similar decision logic:
;;   (cond
;;     (fraud-check-failed? transaction) false
;;     (amount-exceeds-limit? transaction) false
;;     (insufficient-balance? account) false
;;     :else true)
;;
;; And exemplo5.md shows validation with reasons:
;;   (when-not (valid-amount? amount)
;;     {:valid false :reason "Invalid amount"})
;;
;; This demonstrates how production systems make binary decisions (approve/reject)
;; while providing actionable reasons. The pattern is essential for compliance,
;; user experience, and system observability in financial applications.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Qualified applicant
  (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 10000})
  ;; => {:eligible true, :reason "Applicant meets all criteria"}

  ;; Example 2: Too young
  (check-eligibility {:age 17 :income 50000 :credit-score 700 :employed true :debt 10000})
  ;; => {:eligible false, :reason "Age outside eligible range"}

  ;; Example 3: Too old
  (check-eligibility {:age 75 :income 50000 :credit-score 700 :employed true :debt 10000})
  ;; => {:eligible false, :reason "Age outside eligible range"}

  ;; Example 4: Income too low
  (check-eligibility {:age 30 :income 25000 :credit-score 700 :employed true :debt 5000})
  ;; => {:eligible false, :reason "Income below minimum"}

  ;; Example 5: Credit score too low
  (check-eligibility {:age 30 :income 50000 :credit-score 550 :employed true :debt 10000})
  ;; => {:eligible false, :reason "Credit score too low"}

  ;; Example 6: Not employed
  (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed false :debt 10000})
  ;; => {:eligible false, :reason "Employment required"}

  ;; Example 7: Debt-to-income too high
  ;; $50k income → max $20k debt. $25k debt exceeds limit.
  (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 25000})
  ;; => {:eligible false, :reason "Debt-to-income ratio too high"}

  ;; Example 8: Boundary case - exactly at debt limit
  ;; $50k income → max $20k debt. $20k debt is exactly at limit (OK).
  (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 20000})
  ;; => {:eligible true, :reason "Applicant meets all criteria"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test qualified applicant
  (assert (= (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 10000})
             {:eligible true :reason "Applicant meets all criteria"})
          "Should approve qualified applicant")

  ;; Test age rejection (too young)
  (assert (= (check-eligibility {:age 17 :income 50000 :credit-score 700 :employed true :debt 10000})
             {:eligible false :reason "Age outside eligible range"})
          "Should reject applicant under 18")

  ;; Test age rejection (too old)
  (assert (= (check-eligibility {:age 75 :income 50000 :credit-score 700 :employed true :debt 10000})
             {:eligible false :reason "Age outside eligible range"})
          "Should reject applicant over 70")

  ;; Test income rejection
  (assert (= (check-eligibility {:age 30 :income 25000 :credit-score 700 :employed true :debt 5000})
             {:eligible false :reason "Income below minimum"})
          "Should reject low income")

  ;; Test credit score rejection
  (assert (= (check-eligibility {:age 30 :income 50000 :credit-score 550 :employed true :debt 10000})
             {:eligible false :reason "Credit score too low"})
          "Should reject low credit score")

  ;; Test employment rejection
  (assert (= (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed false :debt 10000})
             {:eligible false :reason "Employment required"})
          "Should reject unemployed applicant")

  ;; Test debt-to-income rejection
  (assert (= (check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 25000})
             {:eligible false :reason "Debt-to-income ratio too high"})
          "Should reject high debt-to-income")

  ;; Test boundary cases
  (assert (= (:eligible (check-eligibility {:age 18 :income 30000 :credit-score 600 :employed true :debt 12000}))
             true)
          "Should approve at minimum thresholds")
  (assert (= (:eligible (check-eligibility {:age 70 :income 100000 :credit-score 800 :employed true :debt 40000}))
             true)
          "Should approve at maximum age")

  (println "✓ All tests passed!"))

;; Run: (-test)
