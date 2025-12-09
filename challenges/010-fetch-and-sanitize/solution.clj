;; =============================================================================
;; 010 - FETCH AND SANITIZE
;; Level: 2/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates operation sequencing - performing multiple steps
;; in order to accomplish a task. The pattern is: validate → fetch → transform → return.
;;
;; In real applications, this would involve:
;; 1. Validating input parameters
;; 2. Fetching data from a database or service
;; 3. Transforming/sanitizing the data
;; 4. Returning a structured response
;;
;; We keep it pure for this exercise by using placeholder data, but the pattern
;; is identical to production code. The key insight is that controllers orchestrate
;; multiple operations, each handling one concern (validation, fetching, transformation).

(ns challenge-010.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

(defn fetch-and-sanitize
  "Validates user ID, fetches user data, and sanitizes sensitive fields.

  This function orchestrates multiple operations:
  1. Validates the user ID is positive
  2. Creates user data (simulating a database fetch)
  3. Removes sensitive fields
  4. Returns structured success or error response

  Parameters:
  - user-id: The ID of the user to fetch (integer)

  Returns: Map with either:
           - {:status :success :user {...}}
           - {:status :error :message \"Invalid user ID\"}"
  [user-id]
  (if (pos? user-id)
    ;; Valid: fetch user data and sanitize
    (let [;; Simulate fetching from database (with password)
          fetched-user {:id user-id
                        :name (str "User " user-id)
                        :email (str "user" user-id "@example.com")
                        :password "secret"}
          ;; Sanitize by removing password
          sanitized-user (dissoc fetched-user :password)]
      ;; Return success response
      {:status :success
       :user sanitized-user})
    ;; Invalid: return error
    {:status :error
     :message "Invalid user ID"}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Operation Sequencing
;;    Controllers orchestrate multiple operations in sequence.
;;    Each operation handles a specific responsibility:
;;
;;    validate → fetch → transform → respond
;;
;;    Benefits:
;;    - Single Responsibility Principle (each step does one thing)
;;    - Easy to test each step independently
;;    - Clear flow of data through the system
;;    - Easy to add/remove steps
;;
;;    Example flow:
;;    1. Validate input (early return if invalid)
;;    2. Fetch data (from DB, API, etc.)
;;    3. Transform data (adapt, enrich, sanitize)
;;    4. Return structured response
;;
;; 2. let for Local Bindings
;;    The let form creates local bindings (variables) in sequence:
;;
;;    (let [x 1
;;          y (+ x 1)
;;          z (* y 2)]
;;      z)
;;    ;; => 4
;;
;;    Each binding can use previous bindings.
;;    Bindings are only visible within the let block.
;;
;;    Use let to:
;;    - Break complex calculations into steps
;;    - Name intermediate values
;;    - Improve code readability
;;    - Avoid repeating calculations
;;
;; 3. Early Return Pattern
;;    In functional programming, we use if/cond for early returns:
;;
;;    (if (invalid? input)
;;      (error-response)     ;; Return immediately
;;      (process input))      ;; Continue processing
;;
;;    This is clearer than nested conditionals:
;;
;;    ;; ❌ Deeply nested
;;    (if (valid? input)
;;      (if (authorized? user)
;;        (if (available? resource)
;;          (process input)
;;          (error "unavailable"))
;;        (error "unauthorized"))
;;      (error "invalid"))
;;
;;    ;; ✅ Guard clauses with cond
;;    (cond
;;      (not (valid? input))        (error "invalid")
;;      (not (authorized? user))    (error "unauthorized")
;;      (not (available? resource)) (error "unavailable")
;;      :else                       (process input))
;;
;; 4. Simulating Side Effects Purely
;;    This function is pure (for learning), but simulates a real controller:
;;
;;    Pure version (this challenge):
;;    (let [user {:id id :name "..."}]  ;; Simulate fetch
;;      ...)
;;
;;    Real version (with side effects):
;;    (let [user (db/fetch-user id)]   ;; Actual database call
;;      ...)
;;
;;    The structure is identical - only the implementation changes.
;;    This makes it easy to reason about the flow even in impure code.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: Validate → Fetch → Transform → Return
;;
;; Real-world usage: The reference code shows similar patterns:
;; - admin-fetch-avatar-by-customer-id!:
;;   1. Transforms input (customer-id → review)
;;   2. Extracts avatar-id
;;   3. Fetches avatar
;;   4. Returns result or error
;;
;; This multi-step orchestration is fundamental to controller design.
;; Each step has a clear purpose and they compose to accomplish the full use case.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid user ID
  (fetch-and-sanitize 1)
  ;; => {:status :success :user {:id 1 :name "User 1" :email "user1@example.com"}}
  ;; Note: :password was removed

  ;; Example 2: Invalid - zero
  (fetch-and-sanitize 0)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 3: Valid - larger ID
  (fetch-and-sanitize 42)
  ;; => {:status :success :user {:id 42 :name "User 42" :email "user42@example.com"}}

  ;; Example 4: Invalid - negative
  (fetch-and-sanitize -5)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 5: Valid - small ID
  (fetch-and-sanitize 5)
  ;; => {:status :success :user {:id 5 :name "User 5" :email "user5@example.com"}}

  ;; Example 6: Invalid - large negative
  (fetch-and-sanitize -999)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 7: Accessing nested result
  (let [result (fetch-and-sanitize 10)]
    (if (= (:status result) :success)
      (get-in result [:user :email])
      nil))
  ;; => "user10@example.com"

  ;; Example 8: Demonstrating password was removed
  ;; If we look at the implementation, we create a :password field,
  ;; but it's removed before returning
  (let [result (fetch-and-sanitize 1)]
    (contains? (:user result) :password))
  ;; => false (password field does not exist in result)
)

;; TESTS
;; -----

(defn -test []
  (assert (= (fetch-and-sanitize 1)
             {:status :success
              :user {:id 1
                     :name "User 1"
                     :email "user1@example.com"}})
          "Valid ID 1 should return success with sanitized user")

  (assert (= (fetch-and-sanitize 0)
             {:status :error
              :message "Invalid user ID"})
          "Zero should return error")

  (assert (= (fetch-and-sanitize 42)
             {:status :success
              :user {:id 42
                     :name "User 42"
                     :email "user42@example.com"}})
          "Valid ID 42 should return success")

  (assert (= (fetch-and-sanitize -5)
             {:status :error
              :message "Invalid user ID"})
          "Negative ID should return error")

  (assert (= (fetch-and-sanitize 100)
             {:status :success
              :user {:id 100
                     :name "User 100"
                     :email "user100@example.com"}})
          "Large valid ID should work")

  ;; Test that password is not in the result
  (let [result (fetch-and-sanitize 5)]
    (assert (= (:status result) :success)
            "Should return success for valid ID")
    (assert (not (contains? (:user result) :password))
            "Password field should be removed from result"))

  ;; Test error structure
  (let [result (fetch-and-sanitize -10)]
    (assert (= (:status result) :error)
            "Status should be :error for invalid ID")
    (assert (contains? result :message)
            "Error result should contain :message key"))

  ;; Test success structure
  (let [result (fetch-and-sanitize 7)]
    (assert (= (:status result) :success)
            "Status should be :success for valid ID")
    (assert (contains? result :user)
            "Success result should contain :user key")
    (assert (= (get-in result [:user :id]) 7)
            "User ID should match input"))

  (println "✓ All tests passed! The fetch-and-sanitize function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
