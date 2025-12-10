# 045 - Orchestrate User Registration

**Level**: 9/18
**Type**: Controller
**Concepts**: Multi-function composition, Sequential operations, Complex orchestration

## Context

User registration systems orchestrate multiple operations: validate input, check email uniqueness, hash passwords, create user records, and generate welcome messages. Each step depends on the previous one, and failure at any point should halt the process with a clear error message.

## Objective

Implement a controller function that orchestrates the complete user registration flow through multiple helper functions, using function composition and error handling.

## Specification

### Input

- `registration-data` (map): Registration data with `:email`, `:password`, `:name`, `:age`

### Output

- (map): Result with `:status` and either `:message`/`:user` (success) or `:message` (error)

### Rules

**Sequential operations (implement as helper functions):**
1. **Validate input**: Email must contain `@`, password length >= 8, name not blank, age >= 18
   - If invalid → `{:status :error :message "Validation failed: {reason}"}`
2. **Check email uniqueness**: Email must not be in list `["taken@example.com" "admin@example.com"]`
   - If taken → `{:status :error :message "Email already registered"}`
3. **Hash password**: Replace password with `"hashed:{password}"` (simulation)
4. **Create user**: Build user map with generated ID
5. **Generate welcome message**: Add welcome message to response

**Success response:**
```clojure
{:status :success
 :message "Welcome, {name}!"
 :user {:id "USER-{hash}" :email "..." :name "..." :age ... :password-hash "hashed:..."}}
```

- Use helper functions for each operation
- Use `or` composition for validation steps
- Function must be pure

## Examples

### Example 1
```clojure
(register-user {:email "new@example.com" :password "secret123" :name "John Doe" :age 25})
;; => {:status :success
;;     :message "Welcome, John Doe!"
;;     :user {:id "USER-..." :email "new@example.com" :name "John Doe" :age 25 :password-hash "hashed:secret123"}}
```

### Example 2
```clojure
(register-user {:email "invalid" :password "secret123" :name "John" :age 25})
;; => {:status :error :message "Validation failed: Invalid email format"}
```

### Example 3
```clojure
(register-user {:email "taken@example.com" :password "secret123" :name "John" :age 25})
;; => {:status :error :message "Email already registered"}
```

## Tips

- Create 5 helper functions: `validate-input`, `check-email-availability`, `hash-password`, `create-user-record`, `build-success-response`
- Validation function checks all rules, returns error or nil
- Use `or` in main function: `(or (validate) (check-email) (process))`
- For success path, thread through transformations with `let` or `->`
- Generate user ID with hash: `(str "USER-" (hash email))`
- Each helper has single responsibility

## Testing your solution

```bash
cd challenges/045-user-registration-flow/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-045.solution)
(challenge-045.solution/-test)
```
