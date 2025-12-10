;; =============================================================================
;; 050 - MULTI-STEP USER FETCH
;; Level: 10/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates multi-step orchestration with error handling.
;; We compose four helper functions (fetch, validate, enrich, format), each
;; with single responsibility. The main function uses `or` composition for
;; fail-fast error handling and sequential let bindings for the success path.
;;
;; The approach separates concerns: each helper handles one operation. This
;; makes testing easy (test each helper independently) and code maintainable
;; (changing validation logic only affects validate-status function).
;;
;; This pattern is fundamental in production controllers: complex use cases
;; are implemented by composing small, focused functions rather than writing
;; one large monolithic function.

(ns challenge-050.solution)

;; SIMULATED DATABASE
;; ------------------

(def user-db
  {"USER-1" {:id "USER-1" :name "Alice" :account-status :active}
   "USER-2" {:id "USER-2" :name "Bob" :account-status :suspended}
   "USER-3" {:id "USER-3" :name "Charlie" :account-status :closed}})

;; HELPER FUNCTIONS
;; ----------------

(defn fetch-user
  "Simulates fetching user from database.

  Parameters:
  - user-id: User ID to fetch

  Returns: User map or nil if not found"
  [user-id]
  (get user-db user-id))

(defn validate-status
  "Validates user account status.

  Parameters:
  - user: User map with :account-status

  Returns: Error map if suspended/closed, nil if active"
  [user]
  (case (:account-status user)
    :active nil  ; No error, continue
    :suspended {:status :error :message "Account is suspended"}
    :closed {:status :error :message "Account is closed"}
    nil))  ; Should not happen, but safe default

(defn enrich-profile
  "Enriches user with profile data (simulated).

  Parameters:
  - user: Base user map

  Returns: User with added :last-login and :preferences"
  [user]
  (assoc user
         :last-login "2024-01-15"
         :preferences {:theme "dark"}))

(defn format-response
  "Builds success response with user data.

  Parameters:
  - enriched-user: User map with profile data

  Returns: Success response map"
  [enriched-user]
  {:status :success
   :user enriched-user})

;; MAIN CONTROLLER
;; ---------------

(defn process-user-fetch
  "Orchestrates multi-step user fetch operation.

  Steps:
  1. Fetch user from database
  2. Validate account status
  3. Enrich with profile data
  4. Format success response

  Parameters:
  - user-id: User ID to fetch

  Returns: Success map or error map

  Fail-fast: returns first error encountered, or success if all steps pass."
  [user-id]
  (or
    ;; Step 1: Fetch user, return error if not found
    (when-not (fetch-user user-id)
      {:status :error :message "User not found"})

    ;; Step 2: Validate status, return error if not active
    (let [user (fetch-user user-id)]
      (validate-status user))

    ;; Steps 3-4: Success path - enrich and format
    (let [user (fetch-user user-id)
          enriched-user (enrich-profile user)
          response (format-response enriched-user)]
      response)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Multi-Step Orchestration
;;    Complex operations are broken into steps:
;;    - fetch → validate → enrich → format
;;    Each step is a separate function with single responsibility.
;;    Benefits:
;;    - Each function is independently testable
;;    - Easy to add/remove/modify steps
;;    - Clear separation of concerns
;;    - Reusable functions (validate-status used elsewhere)
;;
;; 2. Fail-Fast with `or` Composition
;;    `or` evaluates expressions left-to-right, returns first truthy value.
;;    For error handling:
;;    - Validation functions return error map (truthy) or nil (falsy)
;;    - `or` returns first error or evaluates success path
;;    This implements fail-fast: stop at first problem, don't waste work.
;;
;; 3. Single Responsibility Principle
;;    Each helper has one job:
;;    - fetch-user: only database access
;;    - validate-status: only status checking
;;    - enrich-profile: only data enrichment
;;    - format-response: only response formatting
;;    This makes code easier to test, understand, and modify.
;;
;; 4. Repeated Fetch (Trade-off)
;;    Notice we call (fetch-user user-id) three times. This is a trade-off:
;;    - Simpler code (each step is independent)
;;    - vs. Performance (redundant fetches)
;;    In production with real DB, you'd fetch once and pass through steps.
;;    For this educational example, simplicity is prioritized.
;;
;; 5. Error Maps vs Exceptions
;;    We use error maps {:status :error :message "..."} rather than exceptions.
;;    Benefits:
;;    - Easier to test (just check return value)
;;    - Explicit in function signature (returns map)
;;    - Composable with `or` pattern
;;    - No implicit control flow (exceptions are implicit)

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md, exemplo2.md
;;
;; Pattern used: Multi-function orchestration with fail-fast error handling
;;
;; Real-world usage: Production controllers compose operations:
;;   (defn process-request [request]
;;     (or (validate-auth request)
;;         (validate-payload request)
;;         (let [user (fetch-user request)
;;               enriched (enrich-data user)
;;               response (format-response enriched)]
;;           response)))
;;
;; The reference code shows similar patterns where controllers orchestrate
;; multiple helper functions, each with single responsibility, composed to
;; implement complete use cases.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Successful fetch (active user)
  (process-user-fetch "USER-1")
  ;; => {:status :success
  ;;     :user {:id "USER-1"
  ;;            :name "Alice"
  ;;            :account-status :active
  ;;            :last-login "2024-01-15"
  ;;            :preferences {:theme "dark"}}}

  ;; Example 2: User not found
  (process-user-fetch "USER-999")
  ;; => {:status :error :message "User not found"}

  ;; Example 3: Suspended account
  (process-user-fetch "USER-2")
  ;; => {:status :error :message "Account is suspended"}

  ;; Example 4: Closed account
  (process-user-fetch "USER-3")
  ;; => {:status :error :message "Account is closed"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test successful fetch (active user)
  (let [result (process-user-fetch "USER-1")]
    (assert (= (:status result) :success)
            "Should return success for active user")
    (assert (= (get-in result [:user :id]) "USER-1")
            "Should include user ID")
    (assert (= (get-in result [:user :name]) "Alice")
            "Should include user name")
    (assert (= (get-in result [:user :account-status]) :active)
            "Should include account status")
    (assert (= (get-in result [:user :last-login]) "2024-01-15")
            "Should enrich with last-login")
    (assert (= (get-in result [:user :preferences]) {:theme "dark"})
            "Should enrich with preferences"))

  ;; Test user not found
  (assert (= (process-user-fetch "USER-999")
             {:status :error :message "User not found"})
          "Should return error for non-existent user")

  ;; Test suspended account
  (assert (= (process-user-fetch "USER-2")
             {:status :error :message "Account is suspended"})
          "Should return error for suspended account")

  ;; Test closed account
  (assert (= (process-user-fetch "USER-3")
             {:status :error :message "Account is closed"})
          "Should return error for closed account")

  (println "✓ All tests passed!"))

;; Run: (-test)
