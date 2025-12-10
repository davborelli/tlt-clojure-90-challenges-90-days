# 056 - Compose Predicates

**Level**: 12/18
**Type**: Pure Function
**Concepts**: Predicate composition, Higher-order functions, Boolean logic composition

## Context

Complex business rules often require combining multiple conditions. Instead of writing long boolean expressions with nested `and`/`or`, we can compose small predicate functions. This makes rules more testable, reusable, and expressive. For example, "eligible users" might be those who are active AND verified AND have completed onboarding.

## Objective

Implement predicate composition functions that combine multiple predicates using boolean logic, and use them to create complex eligibility checks.

## Specification

### Input

For helper functions:
- `predicates` (collection): Vector of predicate functions `[pred1 pred2 ...]`
- `value`: Value to test against predicates

For main function:
- `user` (map): User data to check eligibility
  ```clojure
  {:age ... :verified boolean :account-status :... :onboarding-complete boolean}
  ```

### Output

For helper functions:
- (boolean): Result of composed predicate

For main function:
- (boolean): `true` if user is eligible, `false` otherwise

### Rules

**Helper functions to implement:**

1. `every-pred?` - Returns true if ALL predicates return true
   - Takes predicates and value
   - Pattern: `(every? (fn [pred] (pred value)) predicates)`

2. `some-pred?` - Returns true if ANY predicate returns true
   - Takes predicates and value
   - Pattern: `(some (fn [pred] (pred value)) predicates)`

**Individual predicates for testing:**
- `adult?` - age >= 18
- `verified?` - :verified is true
- `active?` - :account-status is :active
- `onboarding-complete?` - :onboarding-complete is true

**Main function:**
- `eligible-user?` - Checks if user meets ALL criteria:
  - Must be adult (age >= 18)
  - Must be verified
  - Must have active account status
  - Must have completed onboarding
- Use `every-pred?` to compose individual predicates

## Examples

### Example 1
```clojure
(eligible-user?
  {:age 25
   :verified true
   :account-status :active
   :onboarding-complete true})
;; => true
```

### Example 2
```clojure
(eligible-user?
  {:age 25
   :verified false  ; Not verified
   :account-status :active
   :onboarding-complete true})
;; => false
```

### Example 3
```clojure
(eligible-user?
  {:age 16  ; Underage
   :verified true
   :account-status :active
   :onboarding-complete true})
;; => false
```

## Tips

- Use `every?` to check if all predicates pass: `(every? pred? items)`
- Use `some` to check if any predicate passes: `(some pred? items)`
- Define individual predicates as simple functions: `(defn adult? [user] (>= (:age user) 18))`
- Compose predicates: `(every-pred? [adult? verified? active?] user)`
- Pattern: `(defn every-pred? [preds val] (every? #(% val) preds))`

## Testing your solution

```bash
cd challenges/056-compose-predicates/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-056.solution)
(challenge-056.solution/-test)
```
