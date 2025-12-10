# 030 - Error Handling with OR Composition

**Level**: 6/18
**Type**: Controller
**Concepts**: or composition, Error handling, Fallback patterns

## Context

When orchestrating multiple operations that can fail, the `or` operator provides elegant error handling: try the first operation, if it returns an error, return that error; otherwise try the next, and so on. The last expression provides the success case.

## Objective

Implement a controller function that attempts to fetch a user, with fallback error handling at each step.

## Specification

### Input

- `user-id` (integer): The user ID to fetch

### Output

- (map): Either error or success response

### Rules

1. If user-id <= 0, return `{:status :error :message "Invalid ID"}`
2. If user-id > 1000, return `{:status :error :message "ID out of range"}`
3. If user-id is odd, return `{:status :error :message "User not found"}`
4. Otherwise, return success: `{:status :success :user {:id user-id :name "User X"}}`

### Rules

- Use `or` to chain error checks
- Each check returns error map or nil
- Success is returned if all checks pass (return nil)
- Function must be pure

## Examples

### Example 1
```clojure
(fetch-with-fallback 100)
;; => {:status :success :user {:id 100 :name "User 100"}}
```

### Example 2
```clojure
(fetch-with-fallback 0)
;; => {:status :error :message "Invalid ID"}
```

### Example 3
```clojure
(fetch-with-fallback 2000)
;; => {:status :error :message "ID out of range"}
```

### Example 4
```clojure
(fetch-with-fallback 51)
;; => {:status :error :message "User not found"}
```

## Tips

- Create validation functions returning error or nil
- Use `or` to chain: `(or (check1) (check2) (check3) success)`
- Use `when` for checks (returns nil if false)
- Pattern matches challenge 025 but with different validations

## Testing your solution

```bash
cd challenges/030-error-handling-or/
clj -M solution.clj
```
