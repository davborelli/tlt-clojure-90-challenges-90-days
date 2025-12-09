# 005 - Validate and Fetch

**Level**: 1/18
**Type**: Controller
**Concepts**: Conditional logic, if expressions, Error handling

## Context

Controllers orchestrate business logic by combining multiple operations. A common pattern is validating input before performing an operation, returning either the result or an error.

## Objective

Implement a controller function that validates a user ID and returns user data if valid, or an error message if invalid.

## Specification

### Input

- `user-id` (integer): The ID of the user to fetch

### Output

- (map): Either:
  - Success: `{:status :success :user {:id user-id :name "User X"}}`
  - Error: `{:status :error :message "Invalid user ID"}`

### Rules

- User ID must be positive (> 0) to be valid
- If valid, return success map with user data (use placeholder name "User X" where X is the ID)
- If invalid (≤ 0), return error map with error message
- Function must be pure

## Examples

### Example 1
```clojure
(validate-and-fetch 1)
;; => {:status :success :user {:id 1 :name "User 1"}}
```

### Example 2
```clojure
(validate-and-fetch 0)
;; => {:status :error :message "Invalid user ID"}
```

### Example 3
```clojure
(validate-and-fetch -5)
;; => {:status :error :message "Invalid user ID"}
```

### Example 4
```clojure
(validate-and-fetch 42)
;; => {:status :success :user {:id 42 :name "User 42"}}
```

## Tips

- Use an `if` expression to check if the ID is valid
- The condition should check if `user-id` is positive: `(pos? user-id)`
- The `then` branch returns the success map
- The `else` branch returns the error map
- Use `str` to concatenate "User " with the ID number

## Testing your solution

```bash
cd challenges/005-validate-and-fetch/
clj -M solution.clj
```
