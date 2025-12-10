;; =============================================================================
;; 070 - MULTI-SERVICE ORCHESTRATION
;; Level: 14/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller orchestrates account creation across multiple services:
;; validation, duplicate checking, database creation, preference initialization,
;; email sending, and event logging. Each step can fail, requiring careful
;; error handling to ensure clean failure states.
;;
;; We use threading macros to make the happy path explicit, with early returns
;; for errors. Each service is a pure transformation that either succeeds
;; (returns enriched state) or fails (returns error map).
;;
;; This pattern is fundamental in microservice architectures and distributed
;; systems where a single business operation spans multiple services.

(ns challenge-070.solution
  (:require [clojure.string :as str]))

;; HELPER FUNCTIONS
;; ----------------

(defn validate-account-data
  "Validates account creation data has required fields.

  Checks:
  - Email present and non-empty
  - Name present and non-empty
  - Password present and non-empty

  Parameters:
  - state: Account request

  Returns: State with :validated true, or error map"
  [state]
  (cond
    (str/blank? (:email state))
    {:status :error :step "validate-account-data" :message "Email is required"}

    (str/blank? (:name state))
    {:status :error :step "validate-account-data" :message "Name is required"}

    (str/blank? (:password state))
    {:status :error :step "validate-account-data" :message "Password is required"}

    :else
    (assoc state :validated true)))

(defn check-duplicate-email
  "Simulates checking for duplicate email addresses.

  For testing purposes, emails containing 'duplicate' are considered duplicates.

  Parameters:
  - state: Account state with :email

  Returns: State with :duplicate-check-passed true, or error map"
  [state]
  (if (str/includes? (:email state) "duplicate")
    {:status :error :step "check-duplicate-email" :message "Email already exists"}
    (assoc state :duplicate-check-passed true)))

(defn create-account-record
  "Creates account record with generated ID and timestamp.

  Generates:
  - :account-id from email hash
  - :created-at timestamp

  Parameters:
  - state: Account state

  Returns: State with :account-id and :created-at"
  [state]
  (assoc state
         :account-id (str "ACC-" (hash (:email state)))
         :created-at "2024-01-15"))

(defn initialize-preferences
  "Initializes user preferences with defaults.

  Merges request preferences with system defaults.

  Parameters:
  - state: Account state with :preferences

  Returns: State with :preferences-initialized true"
  [state]
  (let [defaults {:theme "light" :language "en" :notifications true}
        prefs (merge defaults (:preferences state))]
    (assoc state
           :preferences prefs
           :preferences-initialized true)))

(defn send-welcome-email
  "Simulates sending welcome email.

  In production, would trigger actual email service.

  Parameters:
  - state: Account state

  Returns: State with :welcome-email-sent true"
  [state]
  (assoc state :welcome-email-sent true))

(defn log-creation-event
  "Logs account creation event.

  Records event for auditing and analytics.

  Parameters:
  - state: Account state

  Returns: State with :events containing creation event"
  [state]
  (assoc state :events [{:type :account-created
                         :email (:email state)
                         :timestamp (:created-at state)}]))

(defn finalize-response
  "Formats final success response.

  Extracts relevant fields for client response.

  Parameters:
  - state: Complete account state

  Returns: Success response map"
  [state]
  {:status :success
   :account-id (:account-id state)
   :message "Account created successfully"
   :events (:events state)})

;; MAIN CONTROLLER
;; ---------------

(defn create-account
  "Orchestrates account creation across multiple services.

  Workflow:
  1. Validate account data
  2. Check for duplicate email
  3. Create account record
  4. Initialize preferences
  5. Send welcome email
  6. Log creation event
  7. Finalize response

  Short-circuits on validation or duplicate errors.

  Parameters:
  - account-request: Map with :email, :name, :password, :preferences

  Returns: Success map or error map"
  [account-request]
  (let [validated (validate-account-data account-request)]
    (if (= (:status validated) :error)
      validated
      (let [duplicate-check (check-duplicate-email validated)]
        (if (= (:status duplicate-check) :error)
          duplicate-check
          (-> duplicate-check
              create-account-record
              initialize-preferences
              send-welcome-email
              log-creation-event
              finalize-response))))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Service Orchestration
;;    A single business operation (create account) requires multiple services:
;;    - Validation service (check data)
;;    - User service (duplicate check, create account)
;;    - Preferences service (initialize settings)
;;    - Email service (send welcome email)
;;    - Event service (log creation)
;;
;;    The controller orchestrates these services in the correct order,
;;    handling dependencies and errors.
;;
;; 2. Early Return Error Handling
;;    We check for errors after critical steps:
;;      (let [result (risky-operation state)]
;;        (if (= (:status result) :error)
;;          result  ; Return error immediately
;;          (continue-processing result)))
;;
;;    This prevents cascading failures: don't create account if
;;    validation fails, don't send email if creation fails.
;;
;; 3. State Enrichment Pipeline
;;    Each successful step enriches the state:
;;      request
;;      → + :validated
;;      → + :duplicate-check-passed
;;      → + :account-id + :created-at
;;      → + :preferences-initialized
;;      → + :welcome-email-sent
;;      → + :events
;;
;;    Final state contains complete audit trail of what happened.
;;
;; 4. Error Context
;;    Error maps include :step to indicate where failure occurred:
;;      {:status :error :step "check-duplicate-email" :message "..."}
;;    This helps debugging and monitoring (which service failed?).
;;
;; 5. Separation of Concerns
;;    Each helper handles one responsibility:
;;    - validate-account-data: input validation only
;;    - check-duplicate-email: duplicate detection only
;;    - create-account-record: database interaction only
;;    - etc.
;;
;;    This makes testing easier and enables independent scaling
;;    of services in production.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo2.md, exemplo5.md
;;
;; Pattern used: Multi-service orchestration with error handling
;;
;; Real-world usage: The reference shows similar orchestration:
;;   (defn process-transaction [request]
;;     (let [validated (validate-transaction request)]
;;       (if (error? validated)
;;         validated
;;         (-> validated
;;             reserve-funds
;;             execute-transaction
;;             send-notification
;;             log-event
;;             build-response))))
;;
;; Production systems use this pattern for:
;; - User registration (validate, create, email, log)
;; - Order processing (validate, charge, fulfill, notify)
;; - Payment flows (authorize, capture, settle, reconcile)
;; - Document workflows (upload, scan, index, notify)
;;
;; In microservices, each step might be a separate HTTP call.
;; This challenge simulates that with pure functions.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Successful account creation
  (create-account
    {:email "user@example.com"
     :name "John Doe"
     :password "secret123"
     :preferences {:theme "dark"}})
  ;; => {:status :success
  ;;     :account-id "ACC-..."
  ;;     :message "Account created successfully"
  ;;     :events [{:type :account-created :email "user@example.com" :timestamp "2024-01-15"}]}

  ;; Example 2: Validation error - missing email
  (create-account
    {:email ""
     :name "Jane Smith"
     :password "pass123"
     :preferences {}})
  ;; => {:status :error
  ;;     :step "validate-account-data"
  ;;     :message "Email is required"}

  ;; Example 3: Validation error - missing name
  (create-account
    {:email "test@example.com"
     :name ""
     :password "pass123"
     :preferences {}})
  ;; => {:status :error
  ;;     :step "validate-account-data"
  ;;     :message "Name is required"}

  ;; Example 4: Duplicate email error
  (create-account
    {:email "duplicate@example.com"
     :name "Bob Wilson"
     :password "pass123"
     :preferences {}})
  ;; => {:status :error
  ;;     :step "check-duplicate-email"
  ;;     :message "Email already exists"}

  ;; Example 5: Successful with preferences
  (create-account
    {:email "alice@example.com"
     :name "Alice Brown"
     :password "secure456"
     :preferences {:language "es" :notifications false}})
  ;; => {:status :success
  ;;     :account-id "ACC-..."
  ;;     :message "Account created successfully"
  ;;     :events [{:type :account-created :email "alice@example.com" ...}]}
)

;; TESTS
;; -----

(defn -test []
  ;; Test successful account creation
  (let [result (create-account
                 {:email "user@example.com"
                  :name "John Doe"
                  :password "secret123"
                  :preferences {:theme "dark"}})]
    (assert (= (:status result) :success)
            "Should succeed with valid data")
    (assert (some? (:account-id result))
            "Should generate account ID")
    (assert (= (:message result) "Account created successfully")
            "Should have success message")
    (assert (= (count (:events result)) 1)
            "Should have creation event"))

  ;; Test validation errors
  (let [result (create-account
                 {:email "" :name "Jane" :password "pass"})]
    (assert (= (:status result) :error)
            "Should fail with missing email")
    (assert (= (:step result) "validate-account-data")
            "Should indicate validation step")
    (assert (= (:message result) "Email is required")
            "Should have email error message"))

  (let [result (create-account
                 {:email "test@example.com" :name "" :password "pass"})]
    (assert (= (:status result) :error)
            "Should fail with missing name")
    (assert (= (:message result) "Name is required")
            "Should have name error message"))

  (let [result (create-account
                 {:email "test@example.com" :name "Bob" :password ""})]
    (assert (= (:status result) :error)
            "Should fail with missing password")
    (assert (= (:message result) "Password is required")
            "Should have password error message"))

  ;; Test duplicate email error
  (let [result (create-account
                 {:email "duplicate@example.com"
                  :name "Bob Wilson"
                  :password "pass123"
                  :preferences {}})]
    (assert (= (:status result) :error)
            "Should fail with duplicate email")
    (assert (= (:step result) "check-duplicate-email")
            "Should indicate duplicate check step")
    (assert (= (:message result) "Email already exists")
            "Should have duplicate error message"))

  ;; Test preferences merging
  (let [result (create-account
                 {:email "alice@example.com"
                  :name "Alice Brown"
                  :password "secure456"
                  :preferences {:language "es"}})]
    (assert (= (:status result) :success)
            "Should succeed with custom preferences"))

  (println "✓ All tests passed! The create-account function works correctly."))

;; Run: (-test)
