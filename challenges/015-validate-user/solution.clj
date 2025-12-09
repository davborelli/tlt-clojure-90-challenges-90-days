;; =============================================================================
;; 015 - VALIDATE USER
;; Level: 3/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates multi-condition validation using cond. Rather
;; than nested if statements, cond provides a clear, linear way to check multiple
;; conditions and handle each case appropriately.
;;
;; The pattern is: check each validation in order, return an error as soon as
;; one fails, or return success if all pass. This "fail fast" approach is
;; efficient and produces clear error messages for users.
;;
;; We use cond which is like a switch/case that checks arbitrary conditions
;; rather than just equality. Each condition is checked in order until one
;; is true, then its corresponding expression is returned.

(ns challenge-015.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn validate-user
  "Validates a user map against multiple business rules.

  Checks name, email, and age validations. Returns success if all pass,
  or error with specific message for the first failed validation.

  Parameters:
  - user: Map with :name, :email, and :age keys

  Returns: Map with either:
           - {:status :success :user user}
           - {:status :error :message \"...\"}"
  [user]
  (let [{:keys [name email age]} user]
    (cond
      ;; Validation 1: Name must not be empty
      (str/blank? name)
      {:status :error
       :message "Name cannot be empty"}

      ;; Validation 2: Email must contain @
      (not (str/includes? email "@"))
      {:status :error
       :message "Invalid email format"}

      ;; Validation 3: Age must be >= 18
      (< age 18)
      {:status :error
       :message "User must be adult"}

      ;; All validations passed
      :else
      {:status :success
       :user user})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. cond Expression
;;    cond checks multiple conditions and returns the first matching result.
;;
;;    Syntax:
;;    (cond
;;      condition1 result1
;;      condition2 result2
;;      condition3 result3
;;      :else default-result)
;;
;;    Examples:
;;    (cond
;;      (< x 0)  "negative"
;;      (= x 0)  "zero"
;;      (> x 0)  "positive")
;;
;;    (cond
;;      (nil? x)    :null
;;      (string? x) :string
;;      (number? x) :number
;;      :else       :unknown)
;;
;;    Key points:
;;    - Checks conditions in order (top to bottom)
;;    - Returns value of first true condition
;;    - :else is conventional for default case
;;    - If no condition matches and no :else, returns nil
;;
;; 2. cond vs if vs case
;;    Choose the right conditional for the job:
;;
;;    if: Two branches (true/false)
;;    (if (adult? age)
;;      "allowed"
;;      "denied")
;;
;;    cond: Multiple arbitrary conditions
;;    (cond
;;      (< age 13)  "child"
;;      (< age 18)  "teen"
;;      (< age 65)  "adult"
;;      :else       "senior")
;;
;;    case: Multiple equality checks on same value
;;    (case status
;;      :pending   "waiting"
;;      :approved  "ok"
;;      :rejected  "denied"
;;      "unknown")
;;
;;    condp: Like case but with custom predicate
;;    (condp = status
;;      :pending  "waiting"
;;      :approved "ok")
;;
;; 3. Fail Fast Validation Pattern
;;    Check validations in order and return first error:
;;
;;    Benefits:
;;    - Clear error messages (user knows what's wrong)
;;    - Efficient (stops at first failure)
;;    - Easy to add/remove validations
;;    - Explicit validation order
;;
;;    Order matters:
;;    - Check cheap validations first (nil checks, empty strings)
;;    - Check expensive validations last (database queries, API calls)
;;    - Check blocking validations first (if name is missing, no point checking email)
;;
;; 4. Validation Error Messages
;;    Good error messages are:
;;    - Specific: "Name cannot be empty" not "Invalid input"
;;    - Actionable: Tell user what to fix
;;    - User-friendly: Plain language, not technical jargon
;;    - Consistent: Same format across application
;;
;;    Examples:
;;    ❌ "Invalid"
;;    ✅ "Email must contain @ symbol"
;;
;;    ❌ "Error 401"
;;    ✅ "Age must be 18 or older"
;;
;; 5. Multi-Condition Validation Approaches
;;    Different ways to handle multiple validations:
;;
;;    a) cond (our solution):
;;       (cond
;;         (invalid1?) error1
;;         (invalid2?) error2
;;         :else success)
;;
;;    b) Nested if (harder to read):
;;       (if (invalid1?)
;;         error1
;;         (if (invalid2?)
;;           error2
;;           success))
;;
;;    c) and with early return:
;;       (or (when (invalid1?) error1)
;;           (when (invalid2?) error2)
;;           success)
;;
;;    d) Validation functions (for reusability):
;;       (or (validate-name user)
;;           (validate-email user)
;;           (validate-age user)
;;           success)
;;
;;    cond is clearest for inline validations.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: Multi-condition validation with cond
;;
;; Real-world usage: Multiple validations appear in:
;; - Form validation (check all fields)
;; - Business rule enforcement (multiple constraints)
;; - Access control (check permissions, quotas, etc.)
;; - Transaction validation (balance, limits, fraud checks)
;;
;; The reference code shows controllers performing validations before operations,
;; and using cond for complex conditional logic is a fundamental pattern.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid user - all checks pass
  (validate-user {:name "John" :email "john@example.com" :age 25})
  ;; => {:status :success :user {:name "John" :email "john@example.com" :age 25}}

  ;; Example 2: Empty name - first validation fails
  (validate-user {:name "" :email "john@example.com" :age 25})
  ;; => {:status :error :message "Name cannot be empty"}

  ;; Example 3: Invalid email - second validation fails
  (validate-user {:name "John" :email "invalid" :age 25})
  ;; => {:status :error :message "Invalid email format"}

  ;; Example 4: Underage - third validation fails
  (validate-user {:name "John" :email "john@example.com" :age 17})
  ;; => {:status :error :message "User must be adult"}

  ;; Example 5: Multiple failures - returns first error (name)
  (validate-user {:name "" :email "invalid" :age 17})
  ;; => {:status :error :message "Name cannot be empty"}

  ;; Example 6: Exactly 18 - passes age validation
  (validate-user {:name "Jane" :email "jane@test.com" :age 18})
  ;; => {:status :success :user {:name "Jane" :email "jane@test.com" :age 18}}

  ;; Example 7: Blank name (spaces) - fails name validation
  (validate-user {:name "   " :email "test@example.com" :age 30})
  ;; => {:status :error :message "Name cannot be empty"}

  ;; Example 8: Email with @ but unusual format - passes
  (validate-user {:name "Bob" :email "b@c" :age 25})
  ;; => {:status :success :user {:name "Bob" :email "b@c" :age 25}}

  ;; Example 9: Using result to make decisions
  (let [result (validate-user {:name "Alice" :email "alice@example.com" :age 30})]
    (if (= (:status result) :success)
      (println "User is valid:" (:user result))
      (println "Validation failed:" (:message result))))
  ;; Prints: "User is valid: {:name Alice :email alice@example.com :age 30}"
)

;; TESTS
;; -----

(defn -test []
  ;; Test success case
  (assert (= (validate-user {:name "John" :email "john@example.com" :age 25})
             {:status :success
              :user {:name "John" :email "john@example.com" :age 25}})
          "Valid user should return success")

  ;; Test empty name
  (assert (= (validate-user {:name "" :email "john@example.com" :age 25})
             {:status :error
              :message "Name cannot be empty"})
          "Empty name should return name error")

  ;; Test invalid email
  (assert (= (validate-user {:name "John" :email "invalid" :age 25})
             {:status :error
              :message "Invalid email format"})
          "Email without @ should return email error")

  ;; Test underage
  (assert (= (validate-user {:name "John" :email "john@example.com" :age 17})
             {:status :error
              :message "User must be adult"})
          "Age < 18 should return age error")

  ;; Test multiple failures - should return first error
  (assert (= (validate-user {:name "" :email "invalid" :age 17})
             {:status :error
              :message "Name cannot be empty"})
          "Multiple failures should return first error")

  ;; Test exactly 18
  (assert (= (validate-user {:name "Jane" :email "jane@test.com" :age 18})
             {:status :success
              :user {:name "Jane" :email "jane@test.com" :age 18}})
          "Age exactly 18 should pass")

  ;; Test blank name (spaces)
  (assert (= (validate-user {:name "   " :email "test@example.com" :age 30})
             {:status :error
              :message "Name cannot be empty"})
          "Blank name with spaces should fail")

  ;; Test minimal valid email
  (assert (= (validate-user {:name "Bob" :email "b@c" :age 25})
             {:status :success
              :user {:name "Bob" :email "b@c" :age 25}})
          "Minimal email with @ should pass")

  (println "✓ All tests passed! The validate-user function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
