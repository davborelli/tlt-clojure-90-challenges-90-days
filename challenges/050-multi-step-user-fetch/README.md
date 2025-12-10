# 050 - Multi-Step User Fetch

**Level**: 10/18
**Type**: Controller
**Concepts**: Function composition, Multi-step operations, Error handling pipelines

## Context

Controllers often need to orchestrate multiple operations in sequence: fetch data, validate it, enrich it with additional information, and format it for the response. Each step may fail, requiring careful error handling. Composing focused helper functions makes this logic testable and maintainable.

## Objective

Implement a controller that orchestrates a multi-step user fetch operation: find user, validate status, enrich with profile data, and format response.

## Specification

### Input

- `user-id` (string): User ID to fetch
- Implicit: Access to helper functions (fetch-user, validate-status, enrich-profile, format-response)

### Output

- (map): Success response `{:status :success :user {...}}` or error `{:status :error :message "..."}`

### Rules

**Helper functions to implement:**

1. `fetch-user` - Simulates database lookup
   - Returns user map if ID in `#{"USER-1" "USER-2" "USER-3"}`, else `nil`
   - User format: `{:id "..." :name "..." :account-status :...}`

2. `validate-status` - Checks account status
   - Returns `nil` if `:account-status` is `:active`
   - Returns error map `{:status :error :message "Account is suspended"}` if `:suspended`
   - Returns error map `{:status :error :message "Account is closed"}` if `:closed`

3. `enrich-profile` - Adds profile data
   - Adds `:last-login` timestamp and `:preferences` map to user
   - Simulated: `(assoc user :last-login "2024-01-15" :preferences {:theme "dark"})`

4. `format-response` - Builds success response
   - Returns `{:status :success :user enriched-user}`

**Main function:**
- `process-user-fetch` orchestrates: fetch → validate → enrich → format
- Use `or` for error handling (return first error or continue)
- Use `let` or `->` for success path

### Expected Data

```clojure
;; Simulated user database
{"USER-1" {:id "USER-1" :name "Alice" :account-status :active}
 "USER-2" {:id "USER-2" :name "Bob" :account-status :suspended}
 "USER-3" {:id "USER-3" :name "Charlie" :account-status :closed}}
```

## Examples

### Example 1
```clojure
(process-user-fetch "USER-1")
;; => {:status :success
;;     :user {:id "USER-1"
;;            :name "Alice"
;;            :account-status :active
;;            :last-login "2024-01-15"
;;            :preferences {:theme "dark"}}}
```

### Example 2
```clojure
(process-user-fetch "USER-999")
;; => {:status :error :message "User not found"}
```

### Example 3
```clojure
(process-user-fetch "USER-2")
;; => {:status :error :message "Account is suspended"}
```

## Tips

- Define helper functions first, each with single responsibility
- Use `or` for fail-fast: `(or error1 error2 success-path)`
- Use `when-not` for validation errors
- Compose helpers in main function: `(-> (fetch) (validate) (enrich) (format))`
- Each helper returns either data (success) or error map (failure)
- Pattern: `(or (check1) (check2) (process-success))`

## Testing your solution

```bash
cd challenges/050-multi-step-user-fetch/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-050.solution)
(challenge-050.solution/-test)
```
