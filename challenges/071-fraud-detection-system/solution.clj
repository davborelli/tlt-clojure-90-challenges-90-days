;; =============================================================================
;; 071 - FRAUD DETECTION SYSTEM
;; Level: 15/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a multi-signal fraud detection system that analyzes
;; various indicators of fraudulent activity. Each signal contributes a weighted
;; score to an overall fraud score, which determines the risk level and action.
;;
;; The approach evaluates each fraud signal independently, accumulating both
;; the numeric score and descriptive signal list. This provides both a quantitative
;; decision (score → action) and qualitative explanation (signals → reason).
;;
;; This pattern is fundamental in fraud prevention, risk management, and any
;; domain requiring multi-factor anomaly detection with explainable decisions.

(ns challenge-071.solution
  (:require [clojure.string :as str]))

;; HELPER FUNCTIONS
;; ----------------

(defn add-signal
  "Adds fraud signal to result, updating score and signals list.

  Parameters:
  - result: Map with :fraud-score and :signals
  - points: Score to add
  - signal-text: Description of triggered signal

  Returns: Updated result map"
  [result points signal-text]
  (-> result
      (update :fraud-score + points)
      (update :signals conj signal-text)))

(defn calculate-risk-level
  "Determines risk level from fraud score.

  Levels:
  - 0-25: low
  - 26-50: medium
  - 51-75: high
  - 76+: critical

  Parameters:
  - fraud-score: Calculated fraud score (0-100+)

  Returns: Risk level keyword"
  [fraud-score]
  (cond
    (<= fraud-score 25) :low
    (<= fraud-score 50) :medium
    (<= fraud-score 75) :high
    :else :critical))

(defn determine-action
  "Determines action based on risk level.

  Actions:
  - low → approve
  - medium → review
  - high → review
  - critical → block

  Parameters:
  - risk-level: Risk level keyword

  Returns: Action keyword"
  [risk-level]
  (case risk-level
    :low :approve
    :medium :review
    :high :review
    :critical :block))

;; MAIN IMPLEMENTATION
;; -------------------

(defn detect-fraud
  "Analyzes transaction for fraud signals and calculates fraud score.

  Fraud signals (weighted):
  - Large amount (> 10000): +20
  - High-risk country: +30
  - High velocity (> 10 tx/24h): +25
  - Unknown device: +15
  - Amount deviation (> 200%): +20
  - Rapid transactions (< 5 min): +15
  - New account (< 30 days): +10

  Parameters:
  - transaction: Map with fraud detection fields

  Returns: Map with :fraud-score, :risk-level, :action, :signals, :reason"
  [transaction]
  (let [{:keys [amount is-high-risk-country velocity-24h device-known
                amount-deviation-percent time-since-last-tx-minutes
                user-age-days]} transaction

        ;; Accumulate signals and score
        result (cond-> {:fraud-score 0 :signals []}
                 ;; Signal 1: Large amount
                 (> amount 10000)
                 (add-signal 20 "Large transaction amount")

                 ;; Signal 2: High-risk country
                 is-high-risk-country
                 (add-signal 30 "High-risk country")

                 ;; Signal 3: High velocity
                 (> velocity-24h 10)
                 (add-signal 25 "Unusual transaction velocity")

                 ;; Signal 4: Unknown device
                 (not device-known)
                 (add-signal 15 "Unknown device")

                 ;; Signal 5: Amount deviation
                 (> amount-deviation-percent 200)
                 (add-signal 20 "Amount deviation from normal")

                 ;; Signal 6: Rapid transactions
                 (< time-since-last-tx-minutes 5)
                 (add-signal 15 "Rapid successive transactions")

                 ;; Signal 7: New account
                 (< user-age-days 30)
                 (add-signal 10 "New account"))

        fraud-score (:fraud-score result)
        signals (:signals result)
        risk-level (calculate-risk-level fraud-score)
        action (determine-action risk-level)
        reason (if (empty? signals)
                 "No fraud signals detected"
                 (str/join "; " signals))]

    {:fraud-score fraud-score
     :risk-level risk-level
     :action action
     :signals signals
     :reason reason}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-Signal Fraud Detection
;;    No single indicator proves fraud. Instead, we combine multiple weak signals:
;;    - Large amount (not fraud alone, but suspicious)
;;    - High-risk country (legitimate users travel there)
;;    - High velocity (could be legitimate shopping spree)
;;    Each signal adds evidence. Combined, they paint a picture.
;;
;; 2. Weighted Scoring
;;    Different signals have different weights:
;;    - High-risk country: 30 points (strong indicator)
;;    - Unknown device: 15 points (moderate indicator)
;;    - New account: 10 points (weak indicator)
;;    Weights reflect empirical fraud rates for each signal.
;;
;; 3. Explainable AI
;;    The system provides both:
;;    - Quantitative: fraud score, risk level, action
;;    - Qualitative: list of triggered signals, reason text
;;    This makes decisions auditable and explainable to users
;;    ("Your transaction was flagged because: high-risk country; unknown device").
;;
;; 4. Signal Accumulation with cond->
;;    Pattern:
;;      (cond-> initial-state
;;        condition1 (update-fn1)
;;        condition2 (update-fn2))
;;    Each condition independently updates the state. This is cleaner than:
;;      (let [state (if cond1 (update1 state) state)
;;            state (if cond2 (update2 state) state)]
;;        state)
;;
;; 5. Risk Thresholds
;;    Score ranges map to risk levels:
;;    - 0-25: low (approve automatically)
;;    - 26-50: medium (human review)
;;    - 51-75: high (human review)
;;    - 76+: critical (block transaction)
;;    Thresholds balance false positives (blocking legitimate transactions)
;;    vs false negatives (allowing fraud).

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Multi-signal analysis with weighted scoring
;;
;; Real-world usage: The reference shows similar fraud checks:
;;   (defn analyze-fraud-risk [transaction]
;;     (cond-> {:score 0 :flags []}
;;       (high-amount? transaction) (add-flag :amount 20)
;;       (velocity-exceeded? transaction) (add-flag :velocity 25)
;;       (location-mismatch? transaction) (add-flag :location 30)))
;;
;; Production fraud detection systems use this pattern with:
;; - Machine learning scores (additional signal)
;;; - Historical user behavior (baseline for deviations)
;; - Real-time velocity checks (across multiple dimensions)
;; - Network analysis (linked accounts, devices)
;; - Rule engines (business-specific rules)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Clean transaction (no signals)
  (detect-fraud
    {:amount 5000
     :location-country "US"
     :is-high-risk-country false
     :velocity-24h 2
     :device-known true
     :amount-deviation-percent 50
     :time-since-last-tx-minutes 120
     :user-age-days 365})
  ;; => {:fraud-score 0
  ;;     :risk-level :low
  ;;     :action :approve
  ;;     :signals []
  ;;     :reason "No fraud signals detected"}

  ;; Example 2: Multiple fraud signals (critical)
  (detect-fraud
    {:amount 15000
     :location-country "XX"
     :is-high-risk-country true
     :velocity-24h 12
     :device-known false
     :amount-deviation-percent 300
     :time-since-last-tx-minutes 2
     :user-age-days 15})
  ;; => {:fraud-score 135
  ;;     :risk-level :critical
  ;;     :action :block
  ;;     :signals ["Large transaction amount" "High-risk country"
  ;;               "Unusual transaction velocity" "Unknown device"
  ;;               "Amount deviation from normal" "Rapid successive transactions"
  ;;               "New account"]
  ;;     :reason "Large transaction amount; High-risk country; ..."}

  ;; Example 3: Medium risk (review required)
  (detect-fraud
    {:amount 12000
     :location-country "US"
     :is-high-risk-country false
     :velocity-24h 5
     :device-known false
     :amount-deviation-percent 150
     :time-since-last-tx-minutes 60
     :user-age-days 100})
  ;; => {:fraud-score 35
  ;;     :risk-level :medium
  ;;     :action :review
  ;;     :signals ["Large transaction amount" "Unknown device"]
  ;;     :reason "Large transaction amount; Unknown device"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test clean transaction (no signals)
  (let [result (detect-fraud
                 {:amount 5000 :is-high-risk-country false :velocity-24h 2
                  :device-known true :amount-deviation-percent 50
                  :time-since-last-tx-minutes 120 :user-age-days 365})]
    (assert (= (:fraud-score result) 0) "Should have 0 fraud score")
    (assert (= (:risk-level result) :low) "Should be low risk")
    (assert (= (:action result) :approve) "Should approve")
    (assert (empty? (:signals result)) "Should have no signals")
    (assert (= (:reason result) "No fraud signals detected")))

  ;; Test all signals triggered
  (let [result (detect-fraud
                 {:amount 15000 :is-high-risk-country true :velocity-24h 12
                  :device-known false :amount-deviation-percent 300
                  :time-since-last-tx-minutes 2 :user-age-days 15})]
    (assert (= (:fraud-score result) 135) "Should have max fraud score")
    (assert (= (:risk-level result) :critical) "Should be critical risk")
    (assert (= (:action result) :block) "Should block transaction")
    (assert (= (count (:signals result)) 7) "Should have all 7 signals"))

  ;; Test medium risk
  (let [result (detect-fraud
                 {:amount 12000 :is-high-risk-country false :velocity-24h 5
                  :device-known false :amount-deviation-percent 150
                  :time-since-last-tx-minutes 60 :user-age-days 100})]
    (assert (= (:fraud-score result) 35) "Should have medium score")
    (assert (= (:risk-level result) :medium) "Should be medium risk")
    (assert (= (:action result) :review) "Should require review"))

  ;; Test high risk
  (let [result (detect-fraud
                 {:amount 15000 :is-high-risk-country true :velocity-24h 11
                  :device-known true :amount-deviation-percent 100
                  :time-since-last-tx-minutes 60 :user-age-days 100})]
    (assert (= (:fraud-score result) 75) "Should have high score")
    (assert (= (:risk-level result) :high) "Should be high risk")
    (assert (= (:action result) :review) "Should require review"))

  (println "✓ All tests passed! The detect-fraud function works correctly."))

;; Run: (-test)
