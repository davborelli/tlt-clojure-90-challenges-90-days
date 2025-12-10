;; =============================================================================
;; 045 - ORCHESTRATE USER REGISTRATION
;; Level: 9/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller orchestrates a multi-step user registration process,
;; composing several helper functions that each handle one responsibility.
;; We use or composition for validation steps (fail-fast on errors) and
;; sequential let bindings for success-path transformations.
;;
;; The approach separates each operation into its own function:
;; - validate-input: checks all input rules
;; - check-email-availability: verifies email isn't taken
;; - hash-password: simulates password hashing
;; - create-user-record: builds user entity
;; - build-success-response: formats final response
;;
;; This pattern is fundamental in production controllers: complex use cases
;; are decomposed into testable, reusable functions that are composed to
;; implement the complete flow.

(ns challenge-045.solution
  (:require [clojure.string :as str]))

;; HELPER FUNCTIONS
;; ----------------

(def taken-emails #{"taken@example.com" "admin@example.com"})

(defn validate-input
  "Validates registration input fields."
  [data]
  (let [{:keys [email password name age]} data]
    (cond
      (not (str/includes? email "@"))
      {:status :error :message "Validation failed: Invalid email format"}

      (< (count password) 8)
      {:status :error :message "Validation failed: Password too short"}

      (str/blank? name)
      {:status :error :message "Validation failed: Name required"}

      (< age 18)
      {:status :error :message "Validation failed: Must be 18 or older"}

      :else
      nil)))  ; Validation passed

(defn check-email-availability
  "Checks if email is already registered."
  [data]
  (when (contains? taken-emails (:email data))
    {:status :error :message "Email already registered"}))

(defn hash-password
  "Simulates password hashing."
  [password]
  (str "hashed:" password))

(defn create-user-record
  "Creates user record with generated ID."
  [data hashed-password]
  (let [{:keys [email name age]} data
        user-id (str "USER-" (hash email))]
    {:id user-id
     :email email
     :name name
     :age age
     :password-hash hashed-password}))

(defn build-success-response
  "Builds success response with welcome message."
  [user]
  {:status :success
   :message (str "Welcome, " (:name user) "!")
   :user user})

;; MAIN CONTROLLER
;; ---------------

(defn register-user
  "Orchestrates user registration through multiple steps.

  Parameters:
  - registration-data: Map with :email, :password, :name, :age

  Returns: Map with :status and either success or error details"
  [registration-data]
  (or
    ;; Step 1: Validate input
    (validate-input registration-data)

    ;; Step 2: Check email availability
    (check-email-availability registration-data)

    ;; Steps 3-5: Success path (hash password, create user, build response)
    (let [hashed-pwd (hash-password (:password registration-data))
          user-record (create-user-record registration-data hashed-pwd)
          response (build-success-response user-record)]
      response)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Function Composition in Controllers
;;    Complex use cases are decomposed into single-responsibility functions:
;;    - Each function handles one step
;;    - Functions are pure (easier to test)
;;    - Functions are reusable across use cases
;;    - Composition creates the complete flow
;;    This makes code maintainable: changing validation rules only affects
;;    validate-input, not the entire registration flow.
;;
;; 2. or for Fail-Fast Validation
;;    The or operator tries each validation in sequence, returning the first
;;    error. If all validations return nil (pass), or evaluates the success
;;    path. This implements fail-fast: stop at first problem, don't continue
;;    expensive operations if early validation fails.
;;
;; 3. let for Success Path Composition
;;    Once validations pass, we use let to sequence operations:
;;    - hash password (needs password from input)
;;    - create user (needs hashed password)
;;    - build response (needs user record)
;;    Each step depends on the previous one. let makes this dependency clear.
;;
;; 4. Separation of Validation and Processing
;;    Validation functions (validate-input, check-email-availability) return
;;    error maps or nil. Processing functions (hash-password, create-user-record)
;;    return transformed data. This separation makes the code easier to test
;;    and understand: validation vs transformation are distinct concerns.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo2.md
;;
;; Pattern used: Multi-function orchestration with validation and processing
;;
;; Real-world usage: The reference code shows similar orchestration:
;;   (defn register-user! [request]
;;     (let [validated (validate-request request)
;;           authorized (authorize! validated)
;;           user-created (create-user! authorized)
;;           token (generate-token! user-created)]
;;       (build-response token user-created)))
;;
;; This demonstrates how production controllers compose multiple operations,
;; each with single responsibility, to implement complex use cases. The pattern
;; is essential for maintainability: adding a new step (e.g., send email)
;; requires adding one function, not modifying existing logic.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Successful registration
  (register-user {:email "new@example.com"
                  :password "secret123"
                  :name "John Doe"
                  :age 25})
  ;; => {:status :success
  ;;     :message "Welcome, John Doe!"
  ;;     :user {:id "USER-...", :email "new@example.com", ...}}

  ;; Example 2: Invalid email
  (register-user {:email "invalid"
                  :password "secret123"
                  :name "John"
                  :age 25})
  ;; => {:status :error, :message "Validation failed: Invalid email format"}

  ;; Example 3: Password too short
  (register-user {:email "john@example.com"
                  :password "short"
                  :name "John"
                  :age 25})
  ;; => {:status :error, :message "Validation failed: Password too short"}

  ;; Example 4: Blank name
  (register-user {:email "john@example.com"
                  :password "secret123"
                  :name ""
                  :age 25})
  ;; => {:status :error, :message "Validation failed: Name required"}

  ;; Example 5: Underage
  (register-user {:email "john@example.com"
                  :password "secret123"
                  :name "John"
                  :age 16})
  ;; => {:status :error, :message "Validation failed: Must be 18 or older"}

  ;; Example 6: Email already taken
  (register-user {:email "taken@example.com"
                  :password "secret123"
                  :name "John"
                  :age 25})
  ;; => {:status :error, :message "Email already registered"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test successful registration
  (let [result (register-user {:email "new@example.com"
                                :password "secret123"
                                :name "John Doe"
                                :age 25})]
    (assert (= (:status result) :success)
            "Should succeed for valid input")
    (assert (= (:message result) "Welcome, John Doe!")
            "Should include welcome message")
    (assert (= (get-in result [:user :email]) "new@example.com")
            "Should include user email")
    (assert (= (get-in result [:user :name]) "John Doe")
            "Should include user name")
    (assert (str/starts-with? (get-in result [:user :password-hash]) "hashed:")
            "Should hash password"))

  ;; Test validation failures
  (assert (= (:message (register-user {:email "invalid" :password "secret123" :name "John" :age 25}))
             "Validation failed: Invalid email format")
          "Should reject invalid email")

  (assert (= (:message (register-user {:email "john@example.com" :password "short" :name "John" :age 25}))
             "Validation failed: Password too short")
          "Should reject short password")

  (assert (= (:message (register-user {:email "john@example.com" :password "secret123" :name "" :age 25}))
             "Validation failed: Name required")
          "Should reject blank name")

  (assert (= (:message (register-user {:email "john@example.com" :password "secret123" :name "John" :age 16}))
             "Validation failed: Must be 18 or older")
          "Should reject underage")

  ;; Test email availability check
  (assert (= (:message (register-user {:email "taken@example.com" :password "secret123" :name "John" :age 25}))
             "Email already registered")
          "Should reject taken email")

  (assert (= (:message (register-user {:email "admin@example.com" :password "secret123" :name "Admin" :age 30}))
             "Email already registered")
          "Should reject admin email")

  (println "✓ All tests passed!"))

;; Run: (-test)
