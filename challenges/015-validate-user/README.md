# 015 - Validate User

**Level**: 3/18
**Type**: Controller
**Concepts**: Multiple validations, cond expression, Error handling

## Context

Real-world validation often involves checking multiple conditions. Each condition might have a different error message. Using `cond` allows us to check conditions in order and return the first error found, or success if all validations pass.

## Objective

Implement a controller function that validates a user map against multiple rules and returns either success or a specific error.

## Specification

### Input

- `user` (map): A map with `:name`, `:email`, and `:age` keys

### Output

- (map): Either:
  - Success: `{:status :success :user user}`
  - Error: `{:status :error :message "..."}`

### Rules

Validate in this order:
1. Name must not be empty string → "Name cannot be empty"
2. Email must contain @ → "Invalid email format"
3. Age must be >= 18 → "User must be adult"
4. If all validations pass, return success

### Rules

- Check validations in order (name, email, age)
- Return error for first failed validation
- If all pass, return success map with user
- Function must be pure

## Examples

### Example 1
```clojure
(validate-user {:name "John" :email "john@example.com" :age 25})
;; => {:status :success :user {:name "John" :email "john@example.com" :age 25}}
```

### Example 2
```clojure
(validate-user {:name "" :email "john@example.com" :age 25})
;; => {:status :error :message "Name cannot be empty"}
```

### Example 3
```clojure
(validate-user {:name "John" :email "invalid" :age 25})
;; => {:status :error :message "Invalid email format"}
```

### Example 4
```clojure
(validate-user {:name "John" :email "john@example.com" :age 17})
;; => {:status :error :message "User must be adult"}
```

## Tips

- Use `cond` to check multiple conditions in order
- `cond` syntax: `(cond condition1 result1 condition2 result2 :else default)`
- Check for failures first (empty name, invalid email, underage)
- Use `:else` clause for the success case
- Use `str/blank?` to check for empty string
- Use `str/includes?` to check for @ in email

## Testing your solution

```bash
cd challenges/015-validate-user/
clj -M solution.clj
```
