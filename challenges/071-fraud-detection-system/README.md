# 071 - Fraud Detection System

**Level**: 15/18
**Type**: Pure Function
**Concepts**: Complex pattern matching, Multi-signal analysis, Weighted scoring, Fraud detection

## Context

Fraud detection systems analyze multiple signals to identify suspicious transactions: unusual amounts, high-risk locations, velocity (transaction frequency), device fingerprints, and behavioral patterns. Each signal contributes to a fraud score, with different weights based on risk severity.

## Objective

Implement a comprehensive fraud detection function that analyzes multiple fraud signals and calculates a weighted fraud score with detailed reasoning.

## Specification

### Input

- `transaction` (map): Transaction details
  ```clojure
  {:amount ...
   :location-country "..."
   :is-high-risk-country boolean
   :velocity-24h ...           ; transactions in last 24h
   :device-known boolean
   :amount-deviation-percent ... ; deviation from user's average
   :time-since-last-tx-minutes ...
   :user-age-days ...}
  ```

### Output

- (map): Fraud analysis result
  ```clojure
  {:fraud-score ...     ; 0-100
   :risk-level :...     ; :low, :medium, :high, :critical
   :action :...         ; :approve, :review, :block
   :signals [...]       ; list of triggered signals
   :reason "..."}
  ```

### Rules

**Fraud signals (each adds to score):**

1. **Large amount** (amount > 10000): +20 points, signal "Large transaction amount"
2. **High-risk country**: +30 points, signal "High-risk country"
3. **High velocity** (velocity-24h > 10): +25 points, signal "Unusual transaction velocity"
4. **Unknown device**: +15 points, signal "Unknown device"
5. **Amount deviation** (deviation > 200%): +20 points, signal "Amount deviation from normal"
6. **Rapid transactions** (time-since-last < 5 min): +15 points, signal "Rapid successive transactions"
7. **New account** (user-age < 30 days): +10 points, signal "New account"

**Risk level (based on fraud-score):**
- 0-25: `:low`
- 26-50: `:medium`
- 51-75: `:high`
- 76+: `:critical`

**Action (based on risk-level):**
- Low: `:approve`
- Medium: `:review`
- High: `:review`
- Critical: `:block`

**Reason:**
- Concatenate all triggered signals into reason string

## Examples

### Example 1
```clojure
(detect-fraud {:amount 5000 :location-country "US" :is-high-risk-country false :velocity-24h 2 :device-known true :amount-deviation-percent 50 :time-since-last-tx-minutes 120 :user-age-days 365})
;; => {:fraud-score 0 :risk-level :low :action :approve :signals [] :reason "No fraud signals detected"}
```

### Example 2
```clojure
(detect-fraud {:amount 15000 :location-country "XX" :is-high-risk-country true :velocity-24h 12 :device-known false :amount-deviation-percent 300 :time-since-last-tx-minutes 2 :user-age-days 15})
;; => {:fraud-score 135 :risk-level :critical :action :block :signals [...] :reason "..."}
```

## Tips

- Use helper function to check each signal and accumulate score
- Pattern: `(cond-> {:fraud-score 0 :signals []} condition (add-signal score text))`
- Or use reduce over signal checks
- Calculate risk level from final score with cond
- Map risk level to action
- Build reason from signals list

## Testing your solution

```bash
cd challenges/071-fraud-detection-system/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-071.solution)
(challenge-071.solution/-test)
```
