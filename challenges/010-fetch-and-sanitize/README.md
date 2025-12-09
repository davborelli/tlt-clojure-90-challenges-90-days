# 010 - Fetch and Sanitize

**Level**: 2/18
**Type**: Controller
**Concepts**: Function composition, Operation sequencing, Data pipelines

## Context

Controllers often need to perform multiple operations in sequence. A common pattern is fetching data and then transforming it before returning. This challenge combines validation, data fetching, and sanitization.

## Objective

Implement a controller function that validates a user ID, fetches user data, removes sensitive fields, and returns the result.

## Specification

### Input

- `user-id` (integer): The ID of the user to fetch

### Output

- (map): Either:
  - Success: `{:status :success :user {:id user-id :name "User X" :email "userX@example.com"}}`
  - Error: `{:status :error :message "Invalid user ID"}`

### Rules

- User ID must be positive (> 0) to be valid
- If invalid, return error map
- If valid, create user data with:
  - `:id` = user-id
  - `:name` = "User X" where X is the ID
  - `:email` = "userX@example.com" where X is the ID
  - `:password` = "secret" (will be removed)
- Remove `:password` field before returning
- Return success map with sanitized user
- Function must be pure

## Examples

### Example 1
```clojure
(fetch-and-sanitize 1)
;; => {:status :success :user {:id 1 :name "User 1" :email "user1@example.com"}}
```

### Example 2
```clojure
(fetch-and-sanitize 0)
;; => {:status :error :message "Invalid user ID"}
```

### Example 3
```clojure
(fetch-and-sanitize 42)
;; => {:status :success :user {:id 42 :name "User 42" :email "user42@example.com"}}
```

### Example 4
```clojure
(fetch-and-sanitize -5)
;; => {:status :error :message "Invalid user ID"}
```

## Tips

- Break the problem into steps: validate → fetch → sanitize → return
- Use `if` for validation
- Use `dissoc` to remove the :password field
- Use `str` and `str/lower-case` to build email addresses
- Remember to require `[clojure.string :as str]`
- Think about the flow: validate first, then process

## Testing your solution

```bash
cd challenges/010-fetch-and-sanitize/
clj -M solution.clj
```
