;; =============================================================================
;; 061 - CALCULATE RISK SCORE
;; Level: 13/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function demonstrates complex pattern matching using cond to evaluate
;; multiple risk factors in priority order. We check the most critical conditions
;; first (fraud history), then progressively evaluate less severe risk factors.
;;
;; The cond macro evaluates conditions sequentially, returning the value of the
;; first truthy condition. This makes it ideal for risk assessment where we need
;; to categorize inputs based on multiple overlapping criteria.
;;
;; This pattern is fundamental in fraud detection, loan approval, and compliance
;; systems where business rules must be evaluated in strict priority order.

(ns challenge-061.solution)

;; IMPLEMENTATION
;; --------------

(defn calculate-risk-score
  "Calculates risk score based on transaction factors using pattern matching.

  Risk levels (checked in order):
  - Critical: Fraud history present
  - High: Large amount + (unverified OR new account)
  - Medium: Medium-large amount unverified, OR very large amount, OR unverified new account
  - Low: All other cases

  Parameters:
  - transaction: Map with :amount, :user-verified, :account-age-days, :has-fraud-history

  Returns: Keyword - :critical, :high, :medium, or :low"
  [transaction]
  (let [{:keys [amount user-verified account-age-days has-fraud-history]} transaction]
    (cond
      ;; Critical: Fraud history (highest priority)
      has-fraud-history
      :critical

      ;; High: Large transaction + (unverified OR very new account)
      (and (> amount 10000)
           (or (not user-verified)
               (< account-age-days 30)))
      :high

      ;; Medium: Various medium-risk combinations
      (and (> amount 5000) (not user-verified))
      :medium

      (> amount 10000)
      :medium

      (and (not user-verified) (< account-age-days 90))
      :medium

      ;; Low: Default for all other cases
      :else
      :low)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Pattern Matching with cond
;;    The cond macro evaluates multiple condition-value pairs sequentially:
;;      (cond
;;        condition1 value1
;;        condition2 value2
;;        :else default-value)
;;    First truthy condition returns its associated value. This is different
;;    from case which matches on equality, or if which handles only two branches.
;;
;; 2. Priority-Based Evaluation
;;    Order matters in cond! Conditions are checked top-to-bottom:
;;    - Put most critical conditions first (fraud history)
;;    - Put more specific conditions before general ones
;;    - Use :else for the catch-all default case
;;    If we reversed order, a fraudulent transaction might be marked :low!
;;
;; 3. Combining Conditions with and/or
;;    Business rules often require multiple factors:
;;      (and (> amount 10000) (not verified))  ; Both must be true
;;      (or (not verified) (< age 30))         ; Either can be true
;;    Clojure evaluates left-to-right, short-circuiting when possible.
;;
;; 4. Destructuring for Readability
;;    Instead of:
;;      (:amount transaction), (:user-verified transaction)
;;    We use:
;;      (let [{:keys [amount user-verified ...]} transaction]
;;    This makes conditions more readable and reduces repetition.
;;
;; 5. Business Rule Encoding
;;    This pattern encodes business logic as code. Each cond branch represents
;;    a documented business rule. When rules change (new risk factors, different
;;    thresholds), we update the corresponding branch. This makes business logic
;;    explicit and auditable.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Multi-branch cond for risk assessment
;;
;; Real-world usage: The reference shows fraud analysis using cond to check
;; multiple risk indicators in priority order:
;;   (cond
;;     (high-risk-country? location) :blocked
;;     (velocity-exceeded? user) :review
;;     :else :approved)
;;
;; Production systems use this pattern for:
;; - Fraud detection (check multiple fraud signals)
;; - Loan approval (evaluate creditworthiness)
;; - Content moderation (flag inappropriate content)
;; - Compliance checks (verify regulatory requirements)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Low risk - verified user, reasonable amount, good history
  (calculate-risk-score
    {:amount 500
     :user-verified true
     :account-age-days 180
     :has-fraud-history false})
  ;; => :low

  ;; Example 2: High risk - large amount, unverified, new account
  (calculate-risk-score
    {:amount 15000
     :user-verified false
     :account-age-days 20
     :has-fraud-history false})
  ;; => :high

  ;; Example 3: Critical - fraud history overrides everything
  (calculate-risk-score
    {:amount 1000
     :user-verified true
     :account-age-days 5
     :has-fraud-history true})
  ;; => :critical

  ;; Example 4: Medium risk - unverified with medium amount
  (calculate-risk-score
    {:amount 6000
     :user-verified false
     :account-age-days 100
     :has-fraud-history false})
  ;; => :medium

  ;; Example 5: Medium risk - very large amount even if verified
  (calculate-risk-score
    {:amount 12000
     :user-verified true
     :account-age-days 200
     :has-fraud-history false})
  ;; => :medium

  ;; Example 6: Medium risk - unverified new account (even small amount)
  (calculate-risk-score
    {:amount 100
     :user-verified false
     :account-age-days 50
     :has-fraud-history false})
  ;; => :medium
)

;; TESTS
;; -----

(defn -test []
  ;; Test low risk
  (assert (= (calculate-risk-score
               {:amount 500 :user-verified true :account-age-days 180 :has-fraud-history false})
             :low)
          "Low risk: verified, reasonable amount, good history")

  ;; Test high risk
  (assert (= (calculate-risk-score
               {:amount 15000 :user-verified false :account-age-days 20 :has-fraud-history false})
             :high)
          "High risk: large amount, unverified, new account")

  (assert (= (calculate-risk-score
               {:amount 12000 :user-verified true :account-age-days 10 :has-fraud-history false})
             :high)
          "High risk: large amount with very new account even if verified")

  ;; Test critical risk
  (assert (= (calculate-risk-score
               {:amount 1000 :user-verified true :account-age-days 5 :has-fraud-history true})
             :critical)
          "Critical: fraud history overrides all other factors")

  ;; Test medium risk variations
  (assert (= (calculate-risk-score
               {:amount 6000 :user-verified false :account-age-days 100 :has-fraud-history false})
             :medium)
          "Medium risk: unverified with medium-high amount")

  (assert (= (calculate-risk-score
               {:amount 12000 :user-verified true :account-age-days 200 :has-fraud-history false})
             :medium)
          "Medium risk: very large amount even if verified and old account")

  (assert (= (calculate-risk-score
               {:amount 100 :user-verified false :account-age-days 50 :has-fraud-history false})
             :medium)
          "Medium risk: unverified new account even with small amount")

  (println "✓ All tests passed! The calculate-risk-score function works correctly."))

;; Run: (-test)
