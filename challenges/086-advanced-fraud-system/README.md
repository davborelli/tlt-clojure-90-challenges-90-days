# 086 - Advanced Fraud Detection System

**Level**: 18/18
**Type**: Pure Function
**Concepts**: ML-style feature extraction, Risk scoring, Pattern detection, Anomaly detection, Multi-factor analysis

## Context

Fraud detection systems protect financial services, e-commerce platforms, and payment processors from fraudulent transactions. Advanced systems extract features from transaction patterns, user behavior, device fingerprints, and historical data to compute risk scores. They must balance catching fraud (sensitivity) with not blocking legitimate users (specificity).

## Objective

Implement a sophisticated fraud detection system that extracts features, computes risk scores using multiple factors, detects patterns, and provides explainable decisions.

## Specification

### Input

- `transaction` (map): Transaction details
- `user-history` (vector): User's transaction history
- `fraud-patterns` (vector): Known fraud patterns
- `risk-config` (map): Risk scoring configuration

### Output

- (map): Fraud analysis with:
  - `:risk-score` (0-100): Overall risk score
  - `:risk-level` (keyword): :low, :medium, :high, :critical
  - `:triggered-rules` (vector): Rules that fired
  - `:features` (map): Extracted features used in scoring
  - `:recommendation` (keyword): :approve, :review, :reject
  - `:explanation` (string): Human-readable explanation

### Rules

- Extract features: velocity, amount deviation, geo-location, device, time patterns
- Score using weighted factors
- Detect patterns: unusual time, unusual location, amount spike, velocity spike
- Consider user history: first transaction, typical patterns, previous fraud
- Provide explainable AI: show which factors contributed most
- Support configurable thresholds and weights

## Examples

### Example 1: Low risk transaction
```clojure
(analyze-fraud
  {:amount 50 :merchant "Coffee Shop" :location "Home City" :time 1400}
  [{:amount 45 :merchant "Grocery" :location "Home City"}
   {:amount 30 :merchant "Gas" :location "Home City"}]
  []
  {:velocity-window-hours 24 :high-risk-threshold 70})
;; => {:risk-score 15
;;     :risk-level :low
;;     :recommendation :approve
;;     :explanation "Normal transaction pattern"
;;     :features {:amount-deviation 0.2 :velocity-normal true :location-known true}}
```

### Example 2: High risk - velocity and amount
```clojure
(analyze-fraud
  {:amount 5000 :merchant "Electronics" :location "Foreign Country" :time 200}
  [{:amount 50 :time 150}
   {:amount 30 :time 120}
   {:amount 100 :time 90}]
  [{:pattern :high-velocity :threshold 3}]
  {})
;; => {:risk-score 85
;;     :risk-level :critical
;;     :recommendation :reject
;;     :triggered-rules [:high-velocity :unusual-amount :foreign-transaction :odd-hour]
;;     :explanation "Multiple risk factors: High transaction velocity (4 txns in 2 hrs), amount 50x above average, foreign location, unusual time"}
```

## Tips

- Use standard deviation for amount anomaly detection
- Implement time-windowed velocity checks
- Consider device fingerprinting in production
- Use machine learning for sophisticated patterns
- Maintain whitelist of known good merchants/locations
- A/B test fraud rules to measure effectiveness

## Testing your solution

```bash
cd challenges/086-advanced-fraud-system/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-086.solution)
(challenge-086.solution/-test)
```
