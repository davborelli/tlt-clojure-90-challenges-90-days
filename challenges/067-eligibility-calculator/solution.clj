;; =============================================================================
;; 067 - ELIGIBILITY CALCULATOR
;; Level: 14/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a complex insurance eligibility calculator that
;; evaluates multiple factors to determine if an applicant qualifies and at
;; what risk level. The logic follows the fail-fast principle: check rejection
;; conditions first, then calculate risk level and premium multiplier.
;;
;; We use cond for rejection checks (each with specific reason) and a separate
;; cond for risk level determination. Risk level maps to premium multiplier,
;; making pricing transparent and rule-based.
;;
;; This pattern is fundamental in insurance, lending, and any domain where
;; multi-factor scoring determines eligibility and pricing.

(ns challenge-067.solution)

;; HELPER FUNCTION
;; ---------------

(defn calculate-risk-level
  "Determines risk level based on applicant factors.

  Risk levels (checked in order):
  - Critical: Poor health OR (smoker AND age > 60)
  - High: Fair health OR high occupation risk OR (smoker AND age > 45)
  - Medium: Good health OR medium occupation risk OR coverage > 300000
  - Low: All other cases

  Parameters:
  - applicant: Applicant map

  Returns: Risk level keyword"
  [{:keys [health-status occupation-risk coverage-amount smoker age]}]
  (cond
    ;; Critical risk
    (or (= health-status :poor)
        (and smoker (> age 60)))
    :critical

    ;; High risk
    (or (= health-status :fair)
        (= occupation-risk :high)
        (and smoker (> age 45)))
    :high

    ;; Medium risk
    (or (= health-status :good)
        (= occupation-risk :medium)
        (> coverage-amount 300000))
    :medium

    ;; Low risk (default)
    :else
    :low))

;; MAIN IMPLEMENTATION
;; -------------------

(defn calculate-eligibility
  "Calculates insurance eligibility based on applicant factors.

  Checks rejection conditions first (fail-fast):
  - Age > 75
  - Age < 18
  - Poor health + preexisting conditions
  - High occupation risk + high coverage

  If eligible, calculates risk level and premium multiplier:
  - Critical: 3.0x
  - High: 2.0x
  - Medium: 1.5x
  - Low: 1.0x

  Parameters:
  - applicant: Map with :age, :health-status, :occupation-risk,
               :coverage-amount, :has-preexisting, :smoker

  Returns: Map with :eligible, :risk-level, :premium-multiplier, :reason"
  [applicant]
  (let [{:keys [age health-status occupation-risk coverage-amount
                has-preexisting]} applicant]
    (cond
      ;; Auto-rejection conditions
      (> age 75)
      {:eligible false
       :risk-level nil
       :premium-multiplier nil
       :reason "Age exceeds maximum"}

      (< age 18)
      {:eligible false
       :risk-level nil
       :premium-multiplier nil
       :reason "Below minimum age"}

      (and (= health-status :poor) has-preexisting)
      {:eligible false
       :risk-level nil
       :premium-multiplier nil
       :reason "Health risk too high"}

      (and (= occupation-risk :high) (> coverage-amount 500000))
      {:eligible false
       :risk-level nil
       :premium-multiplier nil
       :reason "Occupation risk too high for coverage"}

      ;; Eligible - calculate risk and premium
      :else
      (let [risk-level (calculate-risk-level applicant)
            premium-multiplier (case risk-level
                                 :critical 3.0
                                 :high 2.0
                                 :medium 1.5
                                 :low 1.0)]
        {:eligible true
         :risk-level risk-level
         :premium-multiplier premium-multiplier
         :reason "Approved"}))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-Factor Eligibility Scoring
;;    Real-world eligibility rarely depends on a single factor. This function
;;    combines:
;;    - Age (risk increases with age, limits at extremes)
;;    - Health status (direct risk indicator)
;;    - Occupation risk (external danger factors)
;;    - Coverage amount (higher coverage = higher risk)
;;    - Preexisting conditions (known health issues)
;;    - Smoking status (lifestyle risk factor)
;;
;; 2. Fail-Fast Rejection
;;    Check disqualifying conditions first:
;;    - Saves computation (don't calculate risk if rejected)
;;    - Provides clear rejection reasons
;;    - Faster response for obviously ineligible applicants
;;    This is critical in high-volume systems.
;;
;; 3. Risk-Based Pricing
;;    Premium multiplier scales with risk:
;;    - Low risk: 1.0x (baseline rate)
;;    - Medium risk: 1.5x (50% higher)
;;    - High risk: 2.0x (double)
;;    - Critical risk: 3.0x (triple)
;;    This makes pricing fair and actuarially sound.
;;
;; 4. Explicit Rejection Reasons
;;    Each rejection has a specific reason:
;;    - "Age exceeds maximum"
;;    - "Health risk too high"
;;    This provides:
;;    - Transparency for applicants
;;    - Auditability for regulators
;;    - Feedback for improvement
;;
;; 5. Risk Level Hierarchy
;;    Check risk levels from highest to lowest:
;;    - Critical first (most severe)
;;    - Then high, medium, low
;;    This ensures proper categorization: an applicant meeting
;;    both critical and medium criteria is classified as critical.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md, exemplo5.md
;;
;; Pattern used: Multi-factor scoring with fail-fast rejection
;;
;; Real-world usage: The reference shows similar eligibility checks:
;;   (defn check-eligibility [application]
;;     (cond
;;       (disqualifying-factor-1?) (rejection :reason-1)
;;       (disqualifying-factor-2?) (rejection :reason-2)
;;       :else (calculate-approval-terms application)))
;;
;; Production systems use this pattern for:
;; - Insurance underwriting (health, life, auto)
;; - Loan approvals (mortgage, personal, business)
;; - Credit card applications
;; - Rental applications (tenant screening)
;; - Subscription tier eligibility

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Low risk - young, healthy, safe job
  (calculate-eligibility
    {:age 30
     :health-status :excellent
     :occupation-risk :low
     :coverage-amount 200000
     :has-preexisting false
     :smoker false})
  ;; => {:eligible true
  ;;     :risk-level :low
  ;;     :premium-multiplier 1.0
  ;;     :reason "Approved"}

  ;; Example 2: Rejected - too old
  (calculate-eligibility
    {:age 80
     :health-status :good
     :occupation-risk :low
     :coverage-amount 100000
     :has-preexisting false
     :smoker false})
  ;; => {:eligible false
  ;;     :risk-level nil
  ;;     :premium-multiplier nil
  ;;     :reason "Age exceeds maximum"}

  ;; Example 3: High risk - smoker over 45 with fair health
  (calculate-eligibility
    {:age 50
     :health-status :fair
     :occupation-risk :medium
     :coverage-amount 350000
     :has-preexisting false
     :smoker true})
  ;; => {:eligible true
  ;;     :risk-level :high
  ;;     :premium-multiplier 2.0
  ;;     :reason "Approved"}

  ;; Example 4: Rejected - poor health + preexisting
  (calculate-eligibility
    {:age 45
     :health-status :poor
     :occupation-risk :low
     :coverage-amount 150000
     :has-preexisting true
     :smoker false})
  ;; => {:eligible false
  ;;     :risk-level nil
  ;;     :premium-multiplier nil
  ;;     :reason "Health risk too high"}

  ;; Example 5: Medium risk - good health but high coverage
  (calculate-eligibility
    {:age 40
     :health-status :good
     :occupation-risk :low
     :coverage-amount 400000
     :has-preexisting false
     :smoker false})
  ;; => {:eligible true
  ;;     :risk-level :medium
  ;;     :premium-multiplier 1.5
  ;;     :reason "Approved"}

  ;; Example 6: Critical risk - smoker over 60
  (calculate-eligibility
    {:age 65
     :health-status :good
     :occupation-risk :low
     :coverage-amount 200000
     :has-preexisting false
     :smoker true})
  ;; => {:eligible true
  ;;     :risk-level :critical
  ;;     :premium-multiplier 3.0
  ;;     :reason "Approved"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test low risk approval
  (let [result (calculate-eligibility
                 {:age 30 :health-status :excellent :occupation-risk :low
                  :coverage-amount 200000 :has-preexisting false :smoker false})]
    (assert (true? (:eligible result)) "Should be eligible")
    (assert (= (:risk-level result) :low) "Should be low risk")
    (assert (= (:premium-multiplier result) 1.0) "Should have 1.0x multiplier")
    (assert (= (:reason result) "Approved") "Should have approval reason"))

  ;; Test age rejection (too old)
  (let [result (calculate-eligibility
                 {:age 80 :health-status :good :occupation-risk :low
                  :coverage-amount 100000 :has-preexisting false :smoker false})]
    (assert (false? (:eligible result)) "Should be ineligible")
    (assert (= (:reason result) "Age exceeds maximum") "Should have age reason"))

  ;; Test age rejection (too young)
  (let [result (calculate-eligibility
                 {:age 15 :health-status :excellent :occupation-risk :low
                  :coverage-amount 50000 :has-preexisting false :smoker false})]
    (assert (false? (:eligible result)) "Should be ineligible")
    (assert (= (:reason result) "Below minimum age") "Should have age reason"))

  ;; Test health rejection
  (let [result (calculate-eligibility
                 {:age 45 :health-status :poor :occupation-risk :low
                  :coverage-amount 150000 :has-preexisting true :smoker false})]
    (assert (false? (:eligible result)) "Should be ineligible")
    (assert (= (:reason result) "Health risk too high") "Should have health reason"))

  ;; Test occupation rejection
  (let [result (calculate-eligibility
                 {:age 35 :health-status :excellent :occupation-risk :high
                  :coverage-amount 600000 :has-preexisting false :smoker false})]
    (assert (false? (:eligible result)) "Should be ineligible")
    (assert (= (:reason result) "Occupation risk too high for coverage")
            "Should have occupation reason"))

  ;; Test high risk approval
  (let [result (calculate-eligibility
                 {:age 50 :health-status :fair :occupation-risk :medium
                  :coverage-amount 350000 :has-preexisting false :smoker true})]
    (assert (true? (:eligible result)) "Should be eligible")
    (assert (= (:risk-level result) :high) "Should be high risk")
    (assert (= (:premium-multiplier result) 2.0) "Should have 2.0x multiplier"))

  ;; Test medium risk approval
  (let [result (calculate-eligibility
                 {:age 40 :health-status :good :occupation-risk :low
                  :coverage-amount 400000 :has-preexisting false :smoker false})]
    (assert (true? (:eligible result)) "Should be eligible")
    (assert (= (:risk-level result) :medium) "Should be medium risk")
    (assert (= (:premium-multiplier result) 1.5) "Should have 1.5x multiplier"))

  ;; Test critical risk approval
  (let [result (calculate-eligibility
                 {:age 65 :health-status :good :occupation-risk :low
                  :coverage-amount 200000 :has-preexisting false :smoker true})]
    (assert (true? (:eligible result)) "Should be eligible")
    (assert (= (:risk-level result) :critical) "Should be critical risk")
    (assert (= (:premium-multiplier result) 3.0) "Should have 3.0x multiplier"))

  (println "✓ All tests passed! The calculate-eligibility function works correctly."))

;; Run: (-test)
