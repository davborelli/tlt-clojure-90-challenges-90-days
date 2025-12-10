# 037 - Calculate Risk Score

**Level**: 8/18
**Type**: Pure Function
**Concepts**: Pattern matching, cond expressions, Business rules, Score calculation

## Context

Financial systems and security applications often need to calculate risk scores based on multiple factors. These scores help automate decisions like loan approvals, transaction verification, or account reviews. The calculation follows business rules that assign different weights to various risk factors.

## Objective

Implement a pure function that calculates a risk score for a user transaction based on amount, user age, and account age.

## Specification

### Input

- `transaction` (map): Transaction data with `:amount`, `:user-age`, `:account-age-days`

### Output

- (map): Map with `:score` (integer) and `:level` (keyword: `:low`, `:medium`, `:high`)

### Rules

**Score Calculation (sum all applicable points):**
- Amount > 10000: +30 points
- Amount > 5000: +20 points
- Amount > 1000: +10 points
- User age < 21: +15 points
- Account age < 30 days: +25 points
- Account age < 90 days: +10 points

**Risk Level:**
- Score 0-20: `:low`
- Score 21-40: `:medium`
- Score 41+: `:high`

- Function must be pure

## Examples

### Example 1
```clojure
(calculate-risk {:amount 500 :user-age 25 :account-age-days 100})
;; => {:score 0 :level :low}
```

### Example 2
```clojure
(calculate-risk {:amount 6000 :user-age 19 :account-age-days 20})
;; => {:score 60 :level :high}
```

### Example 3
```clojure
(calculate-risk {:amount 2000 :user-age 30 :account-age-days 50})
;; => {:score 20 :level :low}
```

## Tips

- Use `cond` to check each condition and accumulate points
- Start with score 0 and add points for each matched rule
- Check amount rules from highest to lowest (use >, not >=)
- Only the highest matching amount rule applies (use `cond`, not multiple `if`)
- Account age rules are additive (both can apply)
- Calculate score first, then determine level with another `cond`

## Testing your solution

```bash
cd challenges/037-calculate-risk-score/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-037.solution)
(challenge-037.solution/-test)
```
