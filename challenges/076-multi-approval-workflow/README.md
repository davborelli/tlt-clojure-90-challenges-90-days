# 076 - Multi-Approval Workflow

**Level**: 16/18
**Type**: Pure Function
**Concepts**: Complex conditional logic, Multi-criteria evaluation, Priority-based decision trees, Business workflow rules

## Context

In enterprise systems, approval workflows often require multiple levels of authorization based on various factors like amount, risk level, department, and requester role. A robust approval system must evaluate all criteria in a specific order and determine the required approval chain.

## Objective

Implement a pure function that determines the approval workflow required for a request, considering amount thresholds, risk levels, department policies, and requester authority.

## Specification

### Input

- `request` (map): Request details
  ```clojure
  {:amount number
   :risk-level keyword (:low | :medium | :high | :critical)
   :department keyword (:finance | :operations | :it | :hr)
   :requester-role keyword (:employee | :manager | :director | :vp | :ceo)
   :has-budget-approval boolean
   :is-emergency boolean}
  ```

### Output

- (map): Approval workflow
  ```clojure
  {:approvers [keywords]
   :approval-type keyword (:auto | :single | :dual | :board)
   :max-days number
   :requires-documentation boolean
   :escalation-required boolean}
  ```

### Rules

**Priority Order (highest to lowest):**

1. **Emergency Critical**: Risk :critical + :is-emergency → Board approval required
2. **High Amount**: Amount > 1000000 → Board approval, documentation required
3. **Critical Risk**: Risk :critical → VP + CFO dual approval
4. **High Risk + High Amount**: Risk :high + amount > 500000 → Director + VP dual approval
5. **CEO Override**: Requester role :ceo → Auto-approved (unless critical risk)
6. **VP Level**: Requester role :vp + amount < 100000 → Auto-approved
7. **Director with Budget**: Role :director + has-budget-approval + amount < 50000 → Single manager approval
8. **Manager Authority**: Role :manager + amount < 25000 → Single director approval
9. **Standard High Amount**: Amount > 250000 → Dual approval (director + vp)
10. **Standard Medium Amount**: Amount > 50000 → Single VP approval
11. **Standard Low Amount**: Amount > 10000 → Single director approval
12. **Department Specific**: IT department + amount < 15000 → Auto-approved
13. **Default**: Single manager approval

**Additional Rules:**
- Documentation required if amount > 100000 OR risk = :high/:critical
- Escalation required if risk = :critical OR amount > 500000
- Max approval days: :auto (1), :single (3), :dual (5), :board (10)

## Examples

### Example 1: Emergency Critical
```clojure
(determine-approval-workflow {:amount 50000
                              :risk-level :critical
                              :department :operations
                              :requester-role :manager
                              :has-budget-approval false
                              :is-emergency true})
;; => {:approvers [:board :cfo :vp]
;;     :approval-type :board
;;     :max-days 10
;;     :requires-documentation true
;;     :escalation-required true}
```

### Example 2: CEO Override
```clojure
(determine-approval-workflow {:amount 75000
                              :risk-level :low
                              :department :finance
                              :requester-role :ceo
                              :has-budget-approval true
                              :is-emergency false})
;; => {:approvers []
;;     :approval-type :auto
;;     :max-days 1
;;     :requires-documentation false
;;     :escalation-required false}
```

### Example 3: Standard Dual Approval
```clojure
(determine-approval-workflow {:amount 300000
                              :risk-level :medium
                              :department :operations
                              :requester-role :employee
                              :has-budget-approval false
                              :is-emergency false})
;; => {:approvers [:director :vp]
;;     :approval-type :dual
;;     :max-days 5
;;     :requires-documentation true
;;     :escalation-required false}
```

## Tips

- Use `cond` with careful priority ordering (most restrictive first)
- Extract helper functions for documentation and escalation checks
- Consider all combinations of risk + amount thresholds
- CEO can auto-approve unless critical risk
- Emergency + critical always goes to board

## Testing your solution

```bash
cd challenges/076-multi-approval-workflow/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-076.solution)
(challenge-076.solution/-test)
```
