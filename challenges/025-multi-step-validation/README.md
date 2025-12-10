# 025 - Multi-Step Validation with Custom Errors

**Level**: 5/18
**Type**: Controller
**Concepts**: Sequential validation, or composition, Custom error messages

## Context

Sometimes validation requires multiple checks, and we want to return the first error encountered. The `or` operator provides elegant short-circuit evaluation: it returns the first truthy value (error) or the last value (success).

## Objective

Implement a controller function that validates user data through multiple checks, returning the first error found or success if all pass.

## Specification

### Input

- `user` (map): Map with `:name`, `:email`, and `:age` keys

### Output

- (map): Either error or success response

### Rules

Validation checks (in order):
1. Name required: If blank, return `{:status :error :message "Name is required"}`
2. Email format: If no @, return `{:status :error :message "Invalid email"}`
3. Age range: If < 18 or > 120, return `{:status :error :message "Invalid age range"}`
4. If all pass: Return `{:status :success :user user}`

### Rules

- Use `or` to short-circuit on first error
- Each validation returns error map or nil
- Function must be pure

## Examples

### Example 1
```clojure
(validate-user-multi {:name "John" :email "john@test.com" :age 25})
;; => {:status :success :user {:name "John" :email "john@test.com" :age 25}}
```

### Example 2
```clojure
(validate-user-multi {:name "" :email "test@test.com" :age 25})
;; => {:status :error :message "Name is required"}
```

### Example 3
```clojure
(validate-user-multi {:name "Jane" :email "invalid" :age 25})
;; => {:status :error :message "Invalid email"}
```

### Example 4
```clojure
(validate-user-multi {:name "Bob" :email "bob@test.com" :age 150})
;; => {:status :error :message "Invalid age range"}
```

## Tips

- Create validation functions that return error map or nil
- Use `or` to chain: `(or (check1) (check2) (check3) success)`
- `or` returns first truthy value (error) or last value (success)

## Testing your solution

```bash
cd challenges/025-multi-step-validation/
clj -M solution.clj
```
