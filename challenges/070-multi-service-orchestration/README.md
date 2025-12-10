# 070 - Multi-Service Orchestration

**Level**: 14/18
**Type**: Controller
**Concepts**: Service orchestration, Threading macros, Error handling, Multi-step workflow

## Context

Modern applications orchestrate multiple services to complete a business operation. Creating a new account might involve: validating user data, checking for duplicates, creating database record, initializing preferences, sending welcome email, and logging the event. Each step may fail, requiring careful error handling.

## Objective

Implement a controller that orchestrates account creation across multiple services using threading macros and comprehensive error handling.

## Specification

### Input

- `account-request` (map): Account creation request
  ```clojure
  {:email "..."
   :name "..."
   :password "..."
   :preferences {...}}
  ```

### Output

- (map): Account creation result
  - Success: `{:status :success :account-id "..." :message "..." :events [...]}`
  - Error: `{:status :error :step "..." :message "..."}`

### Rules

**Helper functions to implement:**

1. `validate-account-data` - Validates required fields
   - Return error if email, name, or password missing
   - Otherwise add `:validated true`

2. `check-duplicate-email` - Simulates duplicate check
   - If email contains "duplicate", return error
   - Otherwise add `:duplicate-check-passed true`

3. `create-account-record` - Creates account
   - Adds `:account-id` (generate: "ACC-" + hash of email)
   - Adds `:created-at` (use "2024-01-15" as mock timestamp)

4. `initialize-preferences` - Sets up preferences
   - Merge request preferences with defaults
   - Adds `:preferences-initialized true`

5. `send-welcome-email` - Simulates email
   - Adds `:welcome-email-sent true`

6. `log-creation-event` - Logs event
   - Adds `:events` with creation event

7. `finalize-response` - Formats final response
   - Returns `{:status :success :account-id ... :message "Account created" :events [...]}`

**Main function:**
- `create-account` - Orchestrates all steps
- Use `->` to thread through pipeline
- Short-circuit on validation or duplicate errors
- Return error map with `:step` indicating where it failed

## Examples

### Example 1 (success)
```clojure
(create-account {:email "user@example.com" :name "John Doe" :password "secret123" :preferences {}})
;; => {:status :success :account-id "ACC-..." :message "Account created" :events [{:type :account-created :email "user@example.com"}]}
```

### Example 2 (validation error)
```clojure
(create-account {:email "" :name "Jane" :password "pass"})
;; => {:status :error :step "validate-account-data" :message "Email is required"}
```

### Example 3 (duplicate error)
```clojure
(create-account {:email "duplicate@example.com" :name "Bob" :password "pass123" :preferences {}})
;; => {:status :error :step "check-duplicate-email" :message "Email already exists"}
```

## Tips

- Use early return pattern for errors
- Pattern: check for `:status :error` after validation steps
- Thread successful path with `->`
- Generate account-id: `(str "ACC-" (hash email))`
- Mock timestamp can be hardcoded
- Each helper takes state map, returns modified state

## Testing your solution

```bash
cd challenges/070-multi-service-orchestration/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-070.solution)
(challenge-070.solution/-test)
```
