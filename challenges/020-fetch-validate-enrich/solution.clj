;; =============================================================================
;; 020 - FETCH, VALIDATE, AND ENRICH USER
;; Level: 4/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller demonstrates a common pattern: orchestrating multiple
;; operations in sequence where each step depends on the previous one.
;; The flow is: validate → fetch → sanitize → enrich → return.
;;
;; We use `if` for early return on validation failure, and `let` for sequential
;; operations when validation passes. Each `let` binding represents one step
;; in the pipeline, making the flow explicit and easy to follow.
;;
;; This structure appears constantly in production controllers:
;; - Validate input (fail fast if invalid)
;; - Fetch data from source (database, API, etc.)
;; - Transform/sanitize (remove sensitive data)
;; - Enrich (add computed fields, metadata)
;; - Return standardized response
;;
;; The key insight is separating concerns: each step does one thing, and
;; the `let` bindings create a clear narrative of what happens in what order.

(ns challenge-020.solution)

;; IMPLEMENTATION
;; --------------

(defn fetch-validate-enrich
  "Orchestrates fetching, validating, and enriching user data.

  Steps:
  1. Validate user-id is positive
  2. Fetch user data (simulated)
  3. Remove sensitive field (password)
  4. Enrich with timestamp
  5. Return success with processed user

  Parameters:
  - user-id: Integer user identifier

  Returns: Map with :status and either :user or :message"
  [user-id]
  ;; Step 1: Validate (fail fast if invalid)
  (if (pos? user-id)
    ;; Steps 2-5: Sequential operations using let
    (let [;; Step 2: Fetch user data (simulated database fetch)
          fetched-user {:id user-id
                        :name (str "User " user-id)
                        :email (str "user" user-id "@example.com")
                        :password "secret123"}

          ;; Step 3: Sanitize - remove sensitive password field
          sanitized-user (dissoc fetched-user :password)

          ;; Step 4: Enrich - add timestamp metadata
          enriched-user (assoc sanitized-user
                               :fetched-at "2024-01-15T10:00:00")]

      ;; Step 5: Return success response
      {:status :success
       :user enriched-user})

    ;; Validation failed - return error
    {:status :error
     :message "Invalid user ID"}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Operation Sequencing with let
;;    let creates a sequence of local bindings, where each binding can use
;;    values from previous bindings. This makes sequential operations clear.
;;
;;    Pattern:
;;    (let [step1 (operation1)
;;          step2 (operation2 step1)
;;          step3 (operation3 step2)]
;;      (final-operation step3))
;;
;;    Examples:
;;    (let [user (fetch-user 1)
;;          cleaned (remove-password user)
;;          enriched (add-timestamp cleaned)]
;;      {:status :success :data enriched})
;;
;;    (let [raw-data (get-data)
;;          parsed (parse raw-data)
;;          validated (validate parsed)
;;          saved (save! validated)]
;;      saved)
;;
;;    Benefits:
;;    - Clear narrative (reads top to bottom)
;;    - Named intermediates (self-documenting)
;;    - Easy to debug (inspect any step)
;;    - Easy to modify (add/remove steps)
;;
;; 2. Fail Fast Pattern
;;    Check preconditions early and return error immediately if they fail.
;;    Don't waste resources on operations that will fail anyway.
;;
;;    Pattern:
;;    (if (invalid? input)
;;      error-response
;;      (do-expensive-operations input))
;;
;;    Examples:
;;    (if (nil? user-id)
;;      {:error "Missing user ID"}
;;      (fetch-from-database user-id))
;;
;;    (cond
;;      (blank? email) {:error "Email required"}
;;      (< age 18)     {:error "Must be adult"}
;;      :else          (create-account email age))
;;
;;    Benefits:
;;    - Performance (skip unnecessary work)
;;    - Clear error messages (know exactly what failed)
;;    - Resource efficiency (don't hit database for invalid input)
;;
;; 3. Controller Pattern
;;    Controllers orchestrate business logic by coordinating multiple
;;    operations. They don't contain complex logic themselves; they
;;    delegate to helper functions and compose them.
;;
;;    Typical controller structure:
;;    1. Validate input
;;    2. Fetch required data
;;    3. Transform/process data
;;    4. Persist changes (if needed)
;;    5. Return standardized response
;;
;;    Example:
;;    (defn create-order-controller [order-data]
;;      (if-let [errors (validate-order order-data)]
;;        {:status :error :errors errors}
;;        (let [user (fetch-user (:user-id order-data))
;;              items (fetch-items (:item-ids order-data))
;;              total (calculate-total items)
;;              order (build-order user items total)
;;              saved (save-order! order)]
;;          {:status :success :order saved})))
;;
;; 4. Data Sanitization
;;    Removing sensitive data before sending to clients or logs.
;;
;;    Common sensitive fields:
;;    - Passwords (even hashed)
;;    - Social security numbers
;;    - Credit card numbers
;;    - API keys/tokens
;;    - Personal health information
;;
;;    Approaches:
;;    ; Remove specific fields:
;;    (dissoc user :password :ssn)
;;
;;    ; Keep only safe fields:
;;    (select-keys user [:id :name :email])
;;
;;    ; Mask sensitive data:
;;    (update user :ssn mask-ssn)  ; "123-45-6789" → "***-**-6789"
;;
;; 5. Data Enrichment
;;    Adding computed fields, metadata, or derived information.
;;
;;    Common enrichments:
;;    - Timestamps (created-at, updated-at)
;;    - Computed fields (full-name from first + last)
;;    - Source tracking (source, version)
;;    - Display formatting (formatted-price, display-date)
;;
;;    Examples:
;;    (assoc user :loaded-at (System/currentTimeMillis))
;;    (assoc order :total (calculate-total items))
;;    (assoc record :version "v2")

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: Multi-step operation sequencing
;;
;; Real-world usage: The reference code shows similar patterns:
;;   (admin-fetch-avatar-by-customer-id! customer-id) =>
;;     1. Fetch customer
;;     2. Extract avatar-id
;;     3. Fetch avatar
;;     4. Return or error
;;
;; In production systems, this appears in:
;; - User registration (validate → create → send email → return)
;; - Order processing (validate → check inventory → charge → fulfill)
;; - Data import (parse → validate → transform → save)
;; - API endpoints (auth → fetch → transform → respond)

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Valid user ID
  (fetch-validate-enrich 1)
  ;; => {:status :success
  ;;     :user {:id 1
  ;;            :name "User 1"
  ;;            :email "user1@example.com"
  ;;            :fetched-at "2024-01-15T10:00:00"}}

  ;; Example 2: Different valid ID
  (fetch-validate-enrich 42)
  ;; => {:status :success
  ;;     :user {:id 42
  ;;            :name "User 42"
  ;;            :email "user42@example.com"
  ;;            :fetched-at "2024-01-15T10:00:00"}}

  ;; Example 3: Invalid - zero
  (fetch-validate-enrich 0)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 4: Invalid - negative
  (fetch-validate-enrich -5)
  ;; => {:status :error :message "Invalid user ID"}

  ;; Example 5: Checking result status
  (let [result (fetch-validate-enrich 1)]
    (if (= (:status result) :success)
      (println "User:" (:user result))
      (println "Error:" (:message result))))

  ;; Example 6: Demonstrating no password in result
  (let [result (fetch-validate-enrich 1)]
    (get-in result [:user :password]))
  ;; => nil (password was removed)

  ;; Example 7: Mapping over multiple IDs
  (map fetch-validate-enrich [1 2 -1 3])
  ;; => ({:status :success :user {...}}
  ;;     {:status :success :user {...}}
  ;;     {:status :error :message "Invalid user ID"}
  ;;     {:status :success :user {...}})

  ;; Example 8: Extracting successful users only
  (->> [1 2 -1 3 0]
       (map fetch-validate-enrich)
       (filter #(= (:status %) :success))
       (map :user))
  ;; => ({:id 1 ...} {:id 2 ...} {:id 3 ...})
)

;; TESTS
;; -----

(defn -test []
  ;; Test valid user ID
  (let [result (fetch-validate-enrich 1)]
    (assert (= (:status result) :success)
            "Should return success status")
    (assert (= (get-in result [:user :id]) 1)
            "Should have correct user ID")
    (assert (= (get-in result [:user :name]) "User 1")
            "Should have correct name")
    (assert (= (get-in result [:user :email]) "user1@example.com")
            "Should have correct email")
    (assert (= (get-in result [:user :fetched-at]) "2024-01-15T10:00:00")
            "Should have timestamp")
    (assert (nil? (get-in result [:user :password]))
            "Password should be removed"))

  ;; Test different valid ID
  (let [result (fetch-validate-enrich 42)]
    (assert (= (:status result) :success)
            "Should return success for ID 42")
    (assert (= (get-in result [:user :id]) 42)
            "Should have ID 42"))

  ;; Test zero (invalid)
  (assert (= (fetch-validate-enrich 0)
             {:status :error :message "Invalid user ID"})
          "Zero should be invalid")

  ;; Test negative (invalid)
  (assert (= (fetch-validate-enrich -5)
             {:status :error :message "Invalid user ID"})
          "Negative should be invalid")

  ;; Test large ID
  (let [result (fetch-validate-enrich 9999)]
    (assert (= (:status result) :success)
            "Should handle large IDs")
    (assert (= (get-in result [:user :id]) 9999)
            "Should preserve large ID"))

  ;; Test no password leak
  (let [results (map fetch-validate-enrich [1 2 3 4 5])]
    (assert (every? #(nil? (get-in % [:user :password])) results)
            "No result should contain password"))

  ;; Test all successful have timestamp
  (let [results (map fetch-validate-enrich [1 2 3])]
    (assert (every? #(= (get-in % [:user :fetched-at])
                        "2024-01-15T10:00:00")
                    results)
            "All successful results should have timestamp"))

  (println "✓ All tests passed! The fetch-validate-enrich function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
