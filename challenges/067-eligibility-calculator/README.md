# 067 - Eligibility Calculator

**Level**: 14/18
**Type**: Pure Function
**Concepts**: Complex business rules, Multi-factor evaluation, Scoring system

## Context

Insurance eligibility systems evaluate applicants based on multiple factors: age, health status, occupation risk, coverage amount, and existing conditions. Each factor contributes to eligibility determination with different weights and thresholds.

## Objective

Implement a pure function that calculates insurance eligibility based on complex, multi-factor business rules.

## Specification

### Input

- `applicant` (map): Applicant details
  ```clojure
  {:age ...
   :health-status :...      ; :excellent, :good, :fair, :poor
   :occupation-risk :...    ; :low, :medium, :high
   :coverage-amount ...
   :has-preexisting boolean
   :smoker boolean}
  ```

### Output

- (map): Eligibility result
  ```clojure
  {:eligible boolean
   :risk-level :...         ; :low, :medium, :high, :critical
   :premium-multiplier ...  ; 1.0 to 3.0
   :reason "..."}
  ```

### Rules

**Auto-rejection (ineligible):**
- Age > 75 → ineligible, reason "Age exceeds maximum"
- Age < 18 → ineligible, reason "Below minimum age"
- Health status :poor AND has-preexisting → ineligible, reason "Health risk too high"
- Occupation risk :high AND coverage > 500000 → ineligible, reason "Occupation risk too high for coverage"

**Eligible with risk calculation:**

**Risk level determination:**
- Critical: Health :poor OR (smoker AND age > 60)
- High: Health :fair OR occupation :high OR (smoker AND age > 45)
- Medium: Health :good OR occupation :medium OR coverage > 300000
- Low: All other cases

**Premium multiplier:**
- Critical: 3.0
- High: 2.0
- Medium: 1.5
- Low: 1.0

**If eligible:**
- Return `{:eligible true :risk-level ... :premium-multiplier ... :reason "Approved"}`

## Examples

### Example 1
```clojure
(calculate-eligibility {:age 30 :health-status :excellent :occupation-risk :low :coverage-amount 200000 :has-preexisting false :smoker false})
;; => {:eligible true :risk-level :low :premium-multiplier 1.0 :reason "Approved"}
```

### Example 2
```clojure
(calculate-eligibility {:age 80 :health-status :good :occupation-risk :low :coverage-amount 100000 :has-preexisting false :smoker false})
;; => {:eligible false :risk-level nil :premium-multiplier nil :reason "Age exceeds maximum"}
```

### Example 3
```clojure
(calculate-eligibility {:age 50 :health-status :fair :occupation-risk :medium :coverage-amount 350000 :has-preexisting false :smoker true})
;; => {:eligible true :risk-level :high :premium-multiplier 2.0 :reason "Approved"}
```

## Tips

- Check rejection conditions first (fail-fast)
- Use cond for risk level (check critical first, then high, etc.)
- Map risk level to multiplier with case or map lookup
- Each rejected case has specific reason
- Destructure applicant with :keys

## Testing your solution

```bash
cd challenges/067-eligibility-calculator/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-067.solution)
(challenge-067.solution/-test)
```
