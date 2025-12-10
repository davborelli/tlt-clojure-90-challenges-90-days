# 042 - Determine Loan Eligibility

**Level**: 9/18
**Type**: Pure Function
**Concepts**: Complex cond branches, Multiple criteria evaluation, Business logic

## Context

Loan approval systems evaluate applicant eligibility based on multiple factors: age, income, credit score, employment status, and existing debt. Each factor contributes to the decision, and the system must explain why an application is approved or rejected.

## Objective

Implement a pure function that determines loan eligibility based on applicant data, returning both a decision and a reason.

## Specification

### Input

- `applicant` (map): Applicant data with `:age`, `:income`, `:credit-score`, `:employed`, `:debt`

### Output

- (map): Decision map with `:eligible` (boolean) and `:reason` (string)

### Rules

**Rejection criteria (check in order, return first match):**
1. Age < 18 or > 70 → reject, reason "Age outside eligible range"
2. Income < 30000 → reject, reason "Income below minimum"
3. Credit score < 600 → reject, reason "Credit score too low"
4. Not employed (`:employed` false) → reject, reason "Employment required"
5. Debt > (income * 0.4) → reject, reason "Debt-to-income ratio too high"

**Approval:**
- If all checks pass → approve, reason "Applicant meets all criteria"

- Function must be pure

## Examples

### Example 1
```clojure
(check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 10000})
;; => {:eligible true :reason "Applicant meets all criteria"}
```

### Example 2
```clojure
(check-eligibility {:age 17 :income 50000 :credit-score 700 :employed true :debt 10000})
;; => {:eligible false :reason "Age outside eligible range"}
```

### Example 3
```clojure
(check-eligibility {:age 30 :income 25000 :credit-score 700 :employed true :debt 5000})
;; => {:eligible false :reason "Income below minimum"}
```

### Example 4
```clojure
(check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 25000})
;; => {:eligible false :reason "Debt-to-income ratio too high"}
```

## Tips

- Use `cond` to check rejection criteria in order
- Extract fields with destructuring: `{:keys [age income ...]}`
- Calculate debt-to-income ratio: `(> debt (* income 0.4))`
- Use compound conditions with `or` for age range: `(or (< age 18) (> age 70))`
- Return map with both `:eligible` boolean and `:reason` string
- Last condition in cond should be `:else` for approval case

## Testing your solution

```bash
cd challenges/042-loan-eligibility/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-042.solution)
(challenge-042.solution/-test)
```
