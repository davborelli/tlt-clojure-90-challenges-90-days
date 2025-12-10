# 046 - Compose Validators

**Level**: 10/18
**Type**: Pure Function
**Concepts**: Function composition, Higher-order functions, Validation patterns

## Context

In production systems, input validation often involves multiple independent checks (email format, age limits, required fields). Rather than writing one large validation function with many conditions, we can compose small, focused validator functions that each check one rule. This makes validation logic testable, reusable, and maintainable.

## Objective

Implement a function that composes multiple validator functions into a single validation pipeline, returning the first error or success if all pass.

## Specification

### Input

- `validators` (collection): Vector of validator functions, each takes data and returns `nil` (pass) or error map
- `data` (map): Data to validate

### Output

- (map): `nil` if all validations pass, or first error map `{:error "..."}` encountered

### Rules

- Each validator function signature: `(fn [data] ...)` → `nil` or `{:error "message"}`
- Validators run in sequence (fail-fast: stop at first error)
- If all validators return `nil`, the composed function returns `nil`
- Use function composition or `reduce` to combine validators
- Function must be pure

## Examples

### Example 1
```clojure
(def validate-required-name
  (fn [data]
    (when (str/blank? (:name data))
      {:error "Name is required"})))

(def validate-age-limit
  (fn [data]
    (when (< (:age data) 18)
      {:error "Must be 18 or older"})))

(def validate-email
  (fn [data]
    (when-not (str/includes? (:email data) "@")
      {:error "Invalid email format"})))

(compose-validators
  [validate-required-name validate-age-limit validate-email]
  {:name "John" :age 25 :email "john@example.com"})
;; => nil (all validations passed)
```

### Example 2
```clojure
(compose-validators
  [validate-required-name validate-age-limit validate-email]
  {:name "" :age 25 :email "john@example.com"})
;; => {:error "Name is required"}
```

### Example 3
```clojure
(compose-validators
  [validate-required-name validate-age-limit validate-email]
  {:name "John" :age 16 :email "john@example.com"})
;; => {:error "Must be 18 or older"}
```

## Tips

- Use `reduce` to iterate through validators, accumulating errors
- Each validator returns `nil` (pass) or error map (fail)
- Stop at first error using `reduced` in reduce
- Or use `some` to find first non-nil result
- Pattern: `(some (fn [validator] (validator data)) validators)`
- Validators are just functions, easy to test individually

## Testing your solution

```bash
cd challenges/046-compose-validators/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-046.solution)
(challenge-046.solution/-test)
```
