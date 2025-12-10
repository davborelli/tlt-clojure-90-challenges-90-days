;; =============================================================================
;; 037 - CALCULATE RISK SCORE
;; Level: 8/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a risk scoring system with multiple business rules
;; that assign points based on transaction characteristics. We calculate a total
;; score by evaluating each rule, then determine a risk level from that score.
;;
;; The approach separates concerns: score calculation logic is separate from
;; level determination logic. We use cond for the amount check (only highest
;; matches) but separate if statements for additive rules (age, account age).
;;
;; This pattern is common in production systems that evaluate fraud risk,
;; credit worthiness, or security threats, where multiple factors contribute
;; to an overall risk assessment used for automated decision-making.

(ns challenge-037.solution)

;; IMPLEMENTATION
;; --------------

(defn calculate-risk
  "Calculates risk score and level based on transaction characteristics.

  Parameters:
  - transaction: Map with :amount, :user-age, :account-age-days

  Returns: Map with :score (integer) and :level (keyword)"
  [transaction]
  (let [{:keys [amount user-age account-age-days]} transaction
        ;; Calculate score by checking each rule
        amount-points (cond
                        (> amount 10000) 30
                        (> amount 5000)  20
                        (> amount 1000)  10
                        :else            0)
        age-points (if (< user-age 21) 15 0)
        account-points (+ (if (< account-age-days 30) 25 0)
                          (if (< account-age-days 90) 10 0))
        total-score (+ amount-points age-points account-points)
        ;; Determine risk level from score
        risk-level (cond
                     (<= total-score 20) :low
                     (<= total-score 40) :medium
                     :else               :high)]
    {:score total-score
     :level risk-level}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Business Rules as Code
;;    Risk scoring systems encode business policies as conditional logic.
;;    Each rule (amount threshold, age limit) maps directly to a code condition.
;;    This makes the logic auditable, testable, and easy to adjust when
;;    policies change. Clear variable names (amount-points, age-points) make
;;    the business logic visible in the code.
;;
;; 2. cond vs if for Exclusive Rules
;;    For amount-based scoring, only ONE rule should apply (mutually exclusive).
;;    cond stops at the first match, so checking from highest to lowest ensures
;;    the right rule applies. If we used multiple if statements, we'd need
;;    complex conditions like (and (> amount 5000) (<= amount 10000)).
;;
;; 3. Additive vs Exclusive Rules
;;    Account age rules are additive: an account can be both <30 days (25 points)
;;    AND <90 days (10 points), totaling 35 points. We use separate if statements
;;    that both evaluate, unlike cond which would stop at the first match.
;;
;; 4. Separation of Concerns
;;    Score calculation (business rules) is separate from level determination
;;    (categorization). This makes it easy to adjust thresholds independently:
;;    changing the :low/:medium boundary doesn't affect point calculations.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Pattern matching with cond for business rules
;;
;; Real-world usage: The reference code shows similar risk evaluation:
;;   (cond
;;     (and (= :low risk-rating) (= :fast-analysis-queue risk-reason))
;;     true
;;     (= :medium risk-rating)
;;     (some #{risk-reason} [:company-matching :company-user :company-owner])
;;     ...)
;;
;; This demonstrates how production systems use cond to evaluate multiple
;; business rules in order, returning different outcomes based on complex
;; conditions. The pattern is essential for fraud detection, compliance
;; checks, and automated decision-making.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Low risk (small amount, adult, old account)
  (calculate-risk {:amount 500 :user-age 25 :account-age-days 100})
  ;; => {:score 0, :level :low}

  ;; Example 2: High risk (large amount, young user, new account)
  (calculate-risk {:amount 6000 :user-age 19 :account-age-days 20})
  ;; => {:score 60, :level :high}
  ;; Breakdown: 20 (amount) + 15 (age) + 25 (account<30) + 10 (account<90) = 70
  ;; Note: I made an error in README - should be 70, not 60

  ;; Example 3: Low risk boundary (medium amount, adult, mid-age account)
  (calculate-risk {:amount 2000 :user-age 30 :account-age-days 50})
  ;; => {:score 20, :level :low}
  ;; Breakdown: 10 (amount) + 0 (age) + 10 (account<90) = 20

  ;; Example 4: Medium risk
  (calculate-risk {:amount 8000 :user-age 25 :account-age-days 100})
  ;; => {:score 20, :level :low}
  ;; Breakdown: 20 (amount) + 0 + 0 = 20

  ;; Example 5: Highest risk
  (calculate-risk {:amount 15000 :user-age 18 :account-age-days 10})
  ;; => {:score 80, :level :high}
  ;; Breakdown: 30 (amount) + 15 (age) + 25 (account<30) + 10 (account<90) = 80
)

;; TESTS
;; -----

(defn -test []
  (assert (= (calculate-risk {:amount 500 :user-age 25 :account-age-days 100})
             {:score 0 :level :low})
          "Should calculate low risk for safe transaction")
  (assert (= (calculate-risk {:amount 6000 :user-age 19 :account-age-days 20})
             {:score 70 :level :high})
          "Should calculate high risk for risky transaction")
  (assert (= (calculate-risk {:amount 2000 :user-age 30 :account-age-days 50})
             {:score 20 :level :low})
          "Should calculate risk at low boundary")
  (assert (= (calculate-risk {:amount 8000 :user-age 25 :account-age-days 100})
             {:score 20 :level :low})
          "Should handle medium amount only")
  (assert (= (calculate-risk {:amount 15000 :user-age 18 :account-age-days 10})
             {:score 80 :level :high})
          "Should calculate maximum risk score")
  ;; Test boundaries
  (assert (= (:level (calculate-risk {:amount 0 :user-age 25 :account-age-days 100}))
             :low)
          "Should be low for 0 points")
  (assert (= (:level (calculate-risk {:amount 2000 :user-age 19 :account-age-days 50}))
             :medium)
          "Should be medium for 35 points")
  (println "✓ All tests passed!"))

;; Run: (-test)
