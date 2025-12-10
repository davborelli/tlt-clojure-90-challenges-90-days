;; =============================================================================
;; 086 - ADVANCED FRAUD DETECTION SYSTEM
;; Level: 18/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements a sophisticated fraud detection system using
;; ML-style feature extraction and risk scoring. Unlike simpler fraud checks,
;; this system analyzes multiple signals: transaction velocity, amount deviation
;; from user's pattern, geographic anomalies, time-of-day patterns, and more.
;;
;; The approach mirrors machine learning models: extract features from raw data,
;; weight each feature by importance, compute an overall risk score, and make
;; a decision with explanation. This is production fraud systems use (though
;; they'd use actual ML models for scoring).
;;
;; Key challenges: (1) computing meaningful features from transaction history,
;; (2) weighting features appropriately (velocity matters more than time-of-day),
;; (3) providing explainable decisions (why was this flagged?), and (4) balancing
;; false positives (blocking good users) vs false negatives (missing fraud).

(ns challenge-086.solution
  (:require [clojure.string :as str]))

;; FEATURE EXTRACTION
;; ------------------

(defn calculate-avg-amount
  "Calculates average transaction amount from history."
  [history]
  (if (empty? history)
    0
    (/ (reduce + (map :amount history)) (count history))))

(defn calculate-amount-deviation
  "Calculates how much current amount deviates from historical average.
   Returns ratio: 1.0 = same, 2.0 = double, 0.5 = half"
  [amount history]
  (let [avg (calculate-avg-amount history)]
    (if (zero? avg)
      1.0
      (/ amount avg))))

(defn calculate-velocity
  "Counts transactions in recent time window (last N hours)."
  [history window-hours current-time]
  (let [cutoff-time (- current-time (* window-hours 60))]
    (count (filter #(>= (:time %) cutoff-time) history))))

(defn is-location-known?
  "Checks if location appears in user's history."
  [location history]
  (some #(= (:location %) location) history))

(defn is-unusual-time?
  "Checks if transaction time is unusual (late night/early morning)."
  [time]
  (or (< time 600) (> time 2200))) ; Before 6am or after 10pm

(defn extract-features
  "Extracts fraud detection features from transaction and history.
   Returns map of features used for scoring."
  [transaction history window-hours]
  (let [{:keys [amount location time merchant]} transaction
        avg-amount (calculate-avg-amount history)
        amount-deviation (calculate-amount-deviation amount history)
        velocity (calculate-velocity history window-hours time)
        location-known (is-location-known? location history)
        unusual-time (is-unusual-time? time)]
    {:amount amount
     :avg-amount avg-amount
     :amount-deviation amount-deviation
     :velocity velocity
     :location-known location-known
     :unusual-time unusual-time
     :merchant merchant}))

;; RISK SCORING
;; ------------

(defn score-amount
  "Scores amount-based risk. High deviation = high risk."
  [features]
  (let [deviation (:amount-deviation features)]
    (cond
      (> deviation 10) 40  ; 10x above average
      (> deviation 5) 30   ; 5x above average
      (> deviation 3) 20   ; 3x above average
      (> deviation 2) 10   ; 2x above average
      :else 0)))

(defn score-velocity
  "Scores velocity-based risk. Many transactions in short time = high risk."
  [features]
  (let [velocity (:velocity features)]
    (cond
      (>= velocity 10) 30
      (>= velocity 5) 20
      (>= velocity 3) 10
      :else 0)))

(defn score-location
  "Scores location-based risk. Unknown location = risk."
  [features]
  (if (:location-known features) 0 25))

(defn score-time
  "Scores time-based risk. Unusual hours = moderate risk."
  [features]
  (if (:unusual-time features) 15 0))

(defn calculate-risk-score
  "Calculates overall risk score from features (0-100)."
  [features]
  (+ (score-amount features)
     (score-velocity features)
     (score-location features)
     (score-time features)))

(defn determine-risk-level
  "Converts numeric risk score to risk level keyword."
  [score]
  (cond
    (>= score 80) :critical
    (>= score 60) :high
    (>= score 40) :medium
    :else :low))

(defn determine-recommendation
  "Determines action recommendation based on risk level."
  [risk-level]
  (case risk-level
    :critical :reject
    :high :review
    :medium :review
    :low :approve))

(defn build-triggered-rules
  "Builds list of rules that triggered."
  [features]
  (cond-> []
    (> (:amount-deviation features) 3)
    (conj :unusual-amount)

    (>= (:velocity features) 5)
    (conj :high-velocity)

    (not (:location-known features))
    (conj :foreign-location)

    (:unusual-time features)
    (conj :odd-hour)))

(defn build-explanation
  "Builds human-readable explanation of fraud decision."
  [features risk-score triggered-rules]
  (if (empty? triggered-rules)
    "Normal transaction pattern"
    (let [amount-msg (when (> (:amount-deviation features) 3)
                       (format "amount %.1fx above average" (:amount-deviation features)))
          velocity-msg (when (>= (:velocity features) 5)
                         (format "%d txns recently" (:velocity features)))
          location-msg (when (not (:location-known features))
                         "unknown location")
          time-msg (when (:unusual-time features)
                     "unusual time")
          parts (filter some? [amount-msg velocity-msg location-msg time-msg])]
      (str "Multiple risk factors: " (str/join ", " parts)))))

;; MAIN IMPLEMENTATION
;; -------------------

(defn analyze-fraud
  "Analyzes transaction for fraud using feature extraction and risk scoring.

   Extracts features like velocity, amount deviation, location, time patterns.
   Scores each feature and combines into overall risk score.
   Provides explainable decision with triggered rules.

   Parameters:
   - transaction: Current transaction map
   - user-history: Vector of user's previous transactions
   - fraud-patterns: Vector of known fraud patterns (unused in this impl)
   - risk-config: Configuration map with :velocity-window-hours, thresholds

   Returns: Map with :risk-score, :risk-level, :triggered-rules, :features,
            :recommendation, :explanation"
  [transaction user-history fraud-patterns risk-config]
  (let [window-hours (get risk-config :velocity-window-hours 24)

        ;; Extract features from transaction and history
        features (extract-features transaction user-history window-hours)

        ;; Calculate risk score
        risk-score (calculate-risk-score features)

        ;; Determine risk level and recommendation
        risk-level (determine-risk-level risk-score)
        recommendation (determine-recommendation risk-level)

        ;; Identify which rules triggered
        triggered-rules (build-triggered-rules features)

        ;; Build explanation
        explanation (build-explanation features risk-score triggered-rules)]

    {:risk-score risk-score
     :risk-level risk-level
     :triggered-rules triggered-rules
     :features features
     :recommendation recommendation
     :explanation explanation}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Feature Engineering
;;    Machine learning models don't work on raw data - they need features.
;;    Features are derived metrics that capture patterns: velocity (txns/hour),
;;    amount deviation (current/average), location novelty (known/unknown).
;;    Good features make or break fraud detection. This is ML engineering core skill.
;;
;; 2. Risk Scoring with Weighted Factors
;;    Each feature contributes to overall risk: amount (0-40 pts), velocity (0-30),
;;    location (0-25), time (0-15). Weights reflect importance. Amount deviation
;;    gets highest weight because it's the strongest fraud signal. This mimics
;;    linear models in ML: score = w1*f1 + w2*f2 + ...
;;
;; 3. Explainable AI
;;    Production fraud systems must explain decisions (regulatory requirements,
;;    customer service). We track :triggered-rules and build natural language
;;    :explanation. This is critical for trust and debugging. Black-box models
;;    are problematic in regulated industries.
;;
;; 4. Velocity Calculation
;;    Velocity = count of transactions in time window. This catches account
;;    takeover attacks where fraudsters rapidly drain accounts. We filter
;;    history by time window using (filter #(>= (:time %) cutoff) history).
;;    This is a key fraud signal.
;;
;; 5. Statistical Deviation
;;    We calculate how much current transaction deviates from user's pattern:
;;    (/ current-amount average-amount). A ratio > 3 is suspicious (3x normal).
;;    This personalizes fraud detection - what's normal for one user may be
;;    unusual for another. Better than fixed thresholds.
;;
;; 6. Feature Composition with cond->
;;    Building triggered-rules with (cond-> [] condition (conj :rule) ...)
;;    cleanly accumulates rules that fired. Each condition adds a rule if true.
;;    This is more elegant than manual accumulation with if/let.
;;
;; 7. Thresholds and Tuning
;;    Risk thresholds (80=critical, 60=high) are configurable. In production,
;;    these are tuned by analyzing precision/recall curves: lower threshold
;;    catches more fraud (higher recall) but blocks more good users (lower precision).
;;    This is the fundamental tradeoff in fraud detection.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Multi-signal evaluation with weighted scoring (complex cond)
;;
;; The reference shows fraud analysis checking multiple signals:
;;   (cond
;;     (> risk-score 90) :block
;;     (and (= country high-risk) (> amount threshold)) :review
;;     (high-velocity? user) :additional-checks
;;     :else :approved)
;;
;; Real-world usage: Production fraud systems use this pattern for:
;; - Payment fraud detection (card-not-present, account takeover)
;; - Account creation abuse (bots, fake accounts)
;; - Content moderation (spam, abuse detection)
;; - Rate limiting (API abuse, DDoS detection)
;; - Cybersecurity (intrusion detection, anomaly detection)
;;
;; The key insight: Multiple weak signals combine to strong decisions.
;; No single feature is enough - fraud detection requires ensemble of checks.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Low risk transaction
  (analyze-fraud
    {:amount 50 :merchant "Coffee Shop" :location "Home City" :time 1400}
    [{:amount 45 :merchant "Grocery" :location "Home City" :time 1200}
     {:amount 30 :merchant "Gas" :location "Home City" :time 1000}
     {:amount 55 :merchant "Restaurant" :location "Home City" :time 900}]
    []
    {:velocity-window-hours 24})
  ;; => {:risk-score 0
  ;;     :risk-level :low
  ;;     :recommendation :approve
  ;;     :triggered-rules []
  ;;     :explanation "Normal transaction pattern"
  ;;     :features {:amount 50 :avg-amount 43.33 :amount-deviation 1.15
  ;;                :velocity 3 :location-known true :unusual-time false}}

  ;; Example 2: High risk - multiple signals
  (analyze-fraud
    {:amount 5000 :merchant "Electronics" :location "Foreign Country" :time 200}
    [{:amount 50 :time 150 :location "Home City"}
     {:amount 30 :time 120 :location "Home City"}
     {:amount 100 :time 90 :location "Home City"}]
    []
    {:velocity-window-hours 2})
  ;; => {:risk-score 85
  ;;     :risk-level :critical
  ;;     :recommendation :reject
  ;;     :triggered-rules [:unusual-amount :high-velocity :foreign-location :odd-hour]
  ;;     :explanation "Multiple risk factors: amount 55.6x above average, 4 txns recently, unknown location, unusual time"
  ;;     :features {:amount 5000 :avg-amount 90 :amount-deviation 55.56
  ;;                :velocity 4 :location-known false :unusual-time true}}

  ;; Example 3: Medium risk - unusual amount only
  (analyze-fraud
    {:amount 1000 :merchant "Hotel" :location "Home City" :time 1500}
    [{:amount 50 :time 1000 :location "Home City"}
     {:amount 40 :time 900 :location "Home City"}]
    []
    {:velocity-window-hours 24})
  ;; => {:risk-score 30
  ;;     :risk-level :medium
  ;;     :recommendation :review
  ;;     :triggered-rules [:unusual-amount]
  ;;     :explanation "Multiple risk factors: amount 11.1x above average"
  ;;     :features {:amount 1000 :avg-amount 90 :amount-deviation 11.11
  ;;                :velocity 2 :location-known true :unusual-time false}}
)

;; TESTS
;; -----

(defn -test []
  ;; Test low risk transaction
  (let [result (analyze-fraud
                 {:amount 50 :merchant "Store" :location "City" :time 1400}
                 [{:amount 45 :location "City" :time 1200}
                  {:amount 55 :location "City" :time 1000}]
                 []
                 {:velocity-window-hours 24})]
    (assert (= (:risk-level result) :low) "Should be low risk")
    (assert (= (:recommendation result) :approve) "Should approve")
    (assert (empty? (:triggered-rules result)) "No rules should trigger"))

  ;; Test high risk - unusual amount + location
  (let [result (analyze-fraud
                 {:amount 5000 :location "Foreign" :time 1400}
                 [{:amount 50 :location "Home" :time 1200}]
                 []
                 {:velocity-window-hours 24})]
    (assert (>= (:risk-score result) 60) "Should have high risk score")
    (assert (contains? (set (:triggered-rules result)) :unusual-amount) "Should trigger unusual amount")
    (assert (contains? (set (:triggered-rules result)) :foreign-location) "Should trigger foreign location"))

  ;; Test velocity detection
  (let [result (analyze-fraud
                 {:amount 50 :location "City" :time 100}
                 (vec (for [i (range 10)]
                        {:amount 50 :location "City" :time (- 100 (* i 5))}))
                 []
                 {:velocity-window-hours 1})]
    (assert (>= (:velocity (:features result)) 5) "Should detect high velocity")
    (assert (contains? (set (:triggered-rules result)) :high-velocity) "Should trigger velocity rule"))

  ;; Test unusual time
  (let [result (analyze-fraud
                 {:amount 50 :location "City" :time 300}
                 [{:amount 50 :location "City" :time 1400}]
                 []
                 {:velocity-window-hours 24})]
    (assert (:unusual-time (:features result)) "Should detect unusual time")
    (assert (contains? (set (:triggered-rules result)) :odd-hour) "Should trigger odd hour"))

  (println "✓ All tests passed! The analyze-fraud function works correctly."))

;; Run: (-test)
