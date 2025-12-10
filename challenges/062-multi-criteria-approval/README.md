# 062 - Multi-Criteria Approval

**Level**: 13/18
**Type**: Pure Function
**Concepts**: Complex cond, Multi-factor decision making, Business rules

## Context

Loan approval systems evaluate multiple criteria: credit score, income, debt-to-income ratio, employment status, and loan amount. Different combinations of these factors result in different approval decisions (approved, manual-review, rejected).

## Objective

Implement a pure function that determines loan approval status based on multiple criteria using complex pattern matching.

## Specification

### Input

- `loan-application` (map): Application details
  ```clojure
  {:credit-score ...
   :annual-income ...
   :debt-to-income-ratio ...  ; percentage (0-100)
   :employment-status :...    ; :employed, :self-employed, :unemployed
   :loan-amount ...}
  ```

### Output

- (keyword): Approval status - `:approved`, `:manual-review`, or `:rejected`

### Rules

**Approved (auto-approval):**
- Credit score >= 750 AND income >= 50000 AND debt-to-income < 30 AND employed = `:approved`
- Credit score >= 800 AND income >= 75000 AND debt-to-income < 40 = `:approved`

**Rejected (auto-rejection):**
- Credit score < 600 = `:rejected`
- Unemployed = `:rejected`
- Debt-to-income >= 50 = `:rejected`
- Loan amount > (income * 5) = `:rejected`

**Manual Review (default):**
- All other cases = `:manual-review`

**Priority:** Check rejection conditions first, then approval, then manual review

## Examples

### Example 1
```clojure
(evaluate-loan-application
  {:credit-score 780
   :annual-income 60000
   :debt-to-income-ratio 25
   :employment-status :employed
   :loan-amount 150000})
;; => :approved
```

### Example 2
```clojure
(evaluate-loan-application
  {:credit-score 550
   :annual-income 40000
   :debt-to-income-ratio 35
   :employment-status :employed
   :loan-amount 100000})
;; => :rejected
```

### Example 3
```clojure
(evaluate-loan-application
  {:credit-score 680
   :annual-income 55000
   :debt-to-income-ratio 32
   :employment-status :self-employed
   :loan-amount 120000})
;; => :manual-review
```

## Tips

- Use `cond` for multi-branch decision logic
- Check rejection conditions first (fail-fast principle)
- Then check approval conditions
- Default to manual review for edge cases
- Use `and` to combine multiple required conditions
- Destructure with `:keys` for readability

## Testing your solution

```bash
cd challenges/062-multi-criteria-approval/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-062.solution)
(challenge-062.solution/-test)
```
