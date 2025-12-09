;; =============================================================================
;; 005 - VALIDATE AND FETCH
;; Level: 1/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates a fundamental pattern: validate-then-operate.
;; Before performing any operation (in this case, "fetching" user data), we
;; validate the input. If validation fails, we return an error; if it succeeds,
;; we perform the operation and return the result.
;;
;; We use a simple if expression to branch between success and error cases.
;; The result is a map with a :status key that clearly indicates whether the
;; operation succeeded or failed, making it easy for calling code to handle
;; both cases.
;;
;; In real applications, this pattern would connect to a database or external
;; service, but we keep it pure for this exercise by using placeholder data.

(ns challenge-005.solution)

;; IMPLEMENTATION
;; --------------

(defn validate-and-fetch
  "Validates a user ID and returns user data if valid, error if invalid.

  Validation rule: User ID must be positive (> 0).

  Parameters:
  - user-id: The ID of the user to fetch (integer)

  Returns: Map with either:
           - {:status :success :user {:id N :name \"User N\"}}
           - {:status :error :message \"Invalid user ID\"}"
  [user-id]
  ;; Check if the user ID is valid (positive number)
  (if (pos? user-id)
    ;; Valid: return success with user data
    {:status :success
     :user {:id user-id
            :name (str "User " user-id)}}
    ;; Invalid: return error with message
    {:status :error
     :message "Invalid user ID"}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. if Expression in Clojure
;;    The if expression has three parts:
;;    (if condition
;;      then-expression
;;      else-expression)
;;
;;    - Returns then-expression if condition is truthy
;;    - Returns else-expression if condition is falsy (nil or false)
;;    - Both branches must return a value (if is an expression, not a statement)
;;
;;    Example:
;;    (if (> 5 3)
;;      "yes"    ;; returned
;;      "no")
;;    ;; => "yes"
;;
;; 2. pos? Predicate
;;    pos? is a built-in predicate that checks if a number is positive (> 0).
;;    It's more readable than (> n 0) and clearly expresses intent.
;;
;;    Other numeric predicates:
;;    - neg? : checks if negative (< 0)
;;    - zero? : checks if exactly zero
;;    - even? : checks if even number
;;    - odd? : checks if odd number
;;
;; 3. Controller Pattern
;;    Controllers orchestrate operations and handle control flow.
;;    Common responsibilities:
;;    - Input validation
;;    - Calling multiple functions in sequence
;;    - Error handling and reporting
;;    - Aggregating results from multiple sources
;;
;;    Controllers typically:
;;    - Have side effects (I/O, database calls) in real apps
;;    - Return structured responses (success/error maps)
;;    - Handle multiple error conditions
;;
;; 4. Result Maps with :status
;;    Returning maps with a :status key is a common pattern for operations
;;    that can succeed or fail. This allows callers to easily check the outcome:
;;
;;    (let [result (validate-and-fetch 5)]
;;      (if (= (:status result) :success)
;;        (println "User:" (:user result))
;;        (println "Error:" (:message result))))
;;
;;    Alternative patterns include:
;;    - Either/Result monads (more functional)
;;    - Throwing exceptions (less common in Clojure)
;;    - Returning nil for errors (loses error information)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: Validate → Return result or error
;;
;; Real-world usage: The reference code shows similar patterns in:
;; - admin-fetch-avatar-by-id!: Validates input, fetches data, returns result
;; - admin-fetch-avatar-by-customer-id!: Validates, transforms, fetches
;;
;; The pattern of returning structured responses with :status or using
;; conditional logic to handle valid/invalid cases is fundamental to
;; controller design in production systems.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid user ID (positive)
  (validate-and-fetch 1)
  ;; => {:status :success :user {:id 1 :name "User 1"}}

  ;; Example 2: Invalid - zero
  (validate-and-fetch 0)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 3: Invalid - negative
  (validate-and-fetch -5)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 4: Valid - larger ID
  (validate-and-fetch 42)
  ;; => {:status :success :user {:id 42 :name "User 42"}}

  ;; Example 5: Valid - small ID
  (validate-and-fetch 1)
  ;; => {:status :success :user {:id 1 :name "User 1"}}

  ;; Example 6: Invalid - large negative
  (validate-and-fetch -999)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 7: Checking the result status
  (let [result (validate-and-fetch 10)]
    (if (= (:status result) :success)
      (str "Found: " (get-in result [:user :name]))
      (str "Failed: " (:message result))))
  ;; => "Found: User 10"
)

;; TESTS
;; -----

(defn -test []
  (assert (= (validate-and-fetch 1)
             {:status :success
              :user {:id 1 :name "User 1"}})
          "Valid ID 1 should return success with user data")

  (assert (= (validate-and-fetch 0)
             {:status :error
              :message "Invalid user ID"})
          "Zero should return error")

  (assert (= (validate-and-fetch -5)
             {:status :error
              :message "Invalid user ID"})
          "Negative ID should return error")

  (assert (= (validate-and-fetch 42)
             {:status :success
              :user {:id 42 :name "User 42"}})
          "Valid ID 42 should return success with correct name")

  (assert (= (validate-and-fetch 100)
             {:status :success
              :user {:id 100 :name "User 100"}})
          "Large valid ID should work correctly")

  (assert (= (validate-and-fetch -1)
             {:status :error
              :message "Invalid user ID"})
          "Negative ID -1 should return error")

  ;; Test that success results have the expected structure
  (let [result (validate-and-fetch 5)]
    (assert (= (:status result) :success)
            "Status should be :success for valid ID")
    (assert (contains? result :user)
            "Success result should contain :user key")
    (assert (= (get-in result [:user :id]) 5)
            "User ID should match input"))

  ;; Test that error results have the expected structure
  (let [result (validate-and-fetch -10)]
    (assert (= (:status result) :error)
            "Status should be :error for invalid ID")
    (assert (contains? result :message)
            "Error result should contain :message key"))

  (println "✓ All tests passed! The validate-and-fetch function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
