# 061 - Calculate Risk Score

**Level**: 13/18
**Type**: Pure Function
**Concepts**: Pattern matching, cond with multiple branches, Complex decision logic

## Context

Risk assessment systems evaluate multiple factors to calculate a risk score. The score considers transaction amount, user verification status, account age, and previous fraud flags. Different combinations of these factors yield different risk ratings (low, medium, high, critical).

## Objective

Implement a pure function that calculates risk score based on multiple criteria using pattern matching with cond.

## Specification

### Input

- `transaction` (map): Transaction details
  ```clojure
  {:amount ...
   :user-verified boolean
   :account-age-days ...
   :has-fraud-history boolean}
  ```

### Output

- (keyword): Risk rating - `:low`, `:medium`, `:high`, or `:critical`

### Rules

**Critical risk (highest priority):**
- Has fraud history = `:critical`

**High risk:**
- Amount > 10000 AND (not verified OR account < 30 days) = `:high`

**Medium risk:**
- Amount > 5000 AND not verified = `:medium`
- Amount > 10000 = `:medium`
- Not verified AND account < 90 days = `:medium`

**Low risk (default):**
- All other cases = `:low`

**Priority:** Check conditions from critical to low (first match wins)

## Examples

### Example 1
```clojure
(calculate-risk-score {:amount 500 :user-verified true :account-age-days 180 :has-fraud-history false})
;; => :low
```

### Example 2
```clojure
(calculate-risk-score {:amount 15000 :user-verified false :account-age-days 20 :has-fraud-history false})
;; => :high
```

### Example 3
```clojure
(calculate-risk-score {:amount 1000 :user-verified true :account-age-days 5 :has-fraud-history true})
;; => :critical
```

### Example 4
```clojure
(calculate-risk-score {:amount 6000 :user-verified false :account-age-days 100 :has-fraud-history false})
;; => :medium
```

## Tips

- Use `cond` to evaluate conditions from highest to lowest priority
- Destructure the transaction map with `:keys`
- Check critical conditions first (fraud history)
- Use `and` to combine multiple conditions
- Remember first matching condition returns immediately
- Default case (`:else`) handles low risk

## Testing your solution

```bash
cd challenges/061-calculate-risk-score/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-061.solution)
(challenge-061.solution/-test)
```
