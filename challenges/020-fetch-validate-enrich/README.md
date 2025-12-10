# 020 - Fetch, Validate, and Enrich User

**Level**: 4/18
**Type**: Controller
**Concepts**: Operation sequencing, let for flow control, Multi-step transformation

## Context

Real-world controllers often perform multiple operations in sequence: fetch data, validate it, transform it, and enrich it with additional information. Each step depends on the previous one, creating a clear data flow pipeline.

## Objective

Implement a controller function that orchestrates multiple operations: validates a user ID, fetches user data, removes sensitive fields, and enriches with a timestamp. Return success with the processed user or error if validation fails.

## Specification

### Input

- `user-id` (integer): The user ID to fetch

### Output

- (map): Either:
  - Success: `{:status :success :user {...}}`
  - Error: `{:status :error :message "..."}`

### Rules

1. **Validate**: Check if user-id is positive (> 0)
   - If invalid, return `{:status :error :message "Invalid user ID"}`
2. **Fetch**: Create a user map with:
   - `:id` (the user-id)
   - `:name` (string "User " + user-id)
   - `:email` (string "user" + user-id + "@example.com")
   - `:password` (string "secret123")
3. **Sanitize**: Remove the `:password` field
4. **Enrich**: Add `:fetched-at` field with value "2024-01-15T10:00:00"
5. **Return**: Success map with the processed user

### Rules

- Perform operations in sequence using `let`
- Each step transforms the data from previous step
- Return error immediately if validation fails
- Function must be pure

## Examples

### Example 1
```clojure
(fetch-validate-enrich 1)
;; => {:status :success
;;     :user {:id 1
;;            :name "User 1"
;;            :email "user1@example.com"
;;            :fetched-at "2024-01-15T10:00:00"}}
```

### Example 2
```clojure
(fetch-validate-enrich 42)
;; => {:status :success
;;     :user {:id 42
;;            :name "User 42"
;;            :email "user42@example.com"
;;            :fetched-at "2024-01-15T10:00:00"}}
```

### Example 3
```clojure
(fetch-validate-enrich 0)
;; => {:status :error :message "Invalid user ID"}
```

### Example 4
```clojure
(fetch-validate-enrich -5)
;; => {:status :error :message "Invalid user ID"}
```

## Tips

- Use `if` to check validation (positive ID)
- Use `let` with multiple bindings for the operation sequence
- Each binding represents one step: fetch, sanitize, enrich
- Use `dissoc` to remove password
- Use `assoc` to add timestamp
- Structure: validate → fetch → sanitize → enrich → return

## Testing your solution

```bash
cd challenges/020-fetch-validate-enrich/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-020.solution)
(challenge-020.solution/-test)
```
