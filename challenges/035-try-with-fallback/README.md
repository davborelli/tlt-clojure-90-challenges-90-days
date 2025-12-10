# 035 - Try Operation with Fallback

**Level**: 7/18
**Type**: Controller
**Concepts**: Error handling, or composition, Fallback patterns, Conditional flow

## Context

In distributed systems, operations can fail for various reasons: network timeouts, service unavailability, or invalid data. A robust controller implements fallback strategies, trying alternative approaches when the primary operation fails. The `or` operator provides elegant error handling by short-circuiting on the first successful result.

## Objective

Implement a controller function that attempts to fetch data from a primary source, falls back to a secondary source if primary fails, and returns a default value if both fail.

## Specification

### Input

- `user-id` (integer): The user ID to fetch

### Output

- (map): Result map with `:status` and `:source` or `:message`

### Rules

- If `user-id` is even and <= 100: Primary succeeds → `{:status :success :source :primary :user {...}}`
- If `user-id` is even and > 100: Primary fails, Secondary succeeds → `{:status :success :source :secondary :user {...}}`
- If `user-id` is odd: Both fail → `{:status :error :message "User not found in any source"}`
- Use `or` composition for fallback logic
- Function must be pure

## Examples

### Example 1
```clojure
(try-fetch-with-fallback 50)
;; => {:status :success :source :primary :user {:id 50 :name "User 50"}}
```

### Example 2
```clojure
(try-fetch-with-fallback 150)
;; => {:status :success :source :secondary :user {:id 150 :name "User 150"}}
```

### Example 3
```clojure
(try-fetch-with-fallback 99)
;; => {:status :error :message "User not found in any source"}
```

## Tips

- Create helper functions for primary and secondary fetches
- Use `when` to return error maps (returns nil on false)
- Use `or` to try primary first, then secondary, then default error
- Primary fetch: succeeds only if even AND <= 100
- Secondary fetch: succeeds only if even (regardless of value)
- Pattern: `(or (try-primary) (try-secondary) (default-error))`

## Testing your solution

```bash
cd challenges/035-try-with-fallback/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-035.solution)
(challenge-035.solution/-test)
```
