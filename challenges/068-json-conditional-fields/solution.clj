;; =============================================================================
;; 068 - JSON CONDITIONAL FIELDS
;; Level: 14/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter builds JSON API responses with conditional field inclusion,
;; a pattern commonly needed when serializing data to JSON. We only include
;; fields that have meaningful (non-nil) values, keeping responses clean and
;; minimizing payload size.
;;
;; The key pattern is `assoc-some`: a helper that only adds a field if its
;; value is non-nil. We thread the response map through conditional additions,
;; making the logic clear and maintainable.
;;
;; This pattern is fundamental in API design where optional fields should be
;; omitted rather than sent as null, improving client-side handling and
;; reducing bandwidth.

(ns challenge-068.solution)

;; HELPER FUNCTION
;; ---------------

(defn assoc-some
  "Associates key-value pair in map only if value is non-nil.

  This prevents adding nil values to maps, keeping them clean
  and avoiding null in JSON responses.

  Parameters:
  - m: Map to update
  - k: Key to add
  - v: Value (only added if non-nil)

  Returns: Updated map (or unchanged if v is nil)"
  [m k v]
  (if (some? v)
    (assoc m k v)
    m))

;; MAIN IMPLEMENTATION
;; -------------------

(defn build-json-response
  "Builds JSON response map with conditional field inclusion.

  Always includes:
  - :status (keyword → string)
  - :userId (renamed from :user-id)

  Conditionally includes (only if non-nil):
  - :successMessage (from :success-message)
  - :errorMessage (from :error-message)
  - :data (from :data)
  - :metadata (from :metadata)

  Parameters:
  - response-data: Raw response map

  Returns: JSON-ready map with camelCase keys and no nil values"
  [response-data]
  (let [{:keys [status user-id success-message error-message data metadata]} response-data]
    (-> {:status (name status)
         :userId user-id}
        (assoc-some :successMessage success-message)
        (assoc-some :errorMessage error-message)
        (assoc-some :data data)
        (assoc-some :metadata metadata))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Conditional Field Inclusion (assoc-some pattern)
;;    Problem: JSON APIs shouldn't send {"field": null} for optional fields
;;    Solution: Only include field if it has a value
;;
;;    Without assoc-some:
;;      {:status "success" :errorMessage nil :data nil}  ; Bad
;;    With assoc-some:
;;      {:status "success"}  ; Clean
;;
;;    Benefits:
;;    - Smaller payloads (less bandwidth)
;;    - Cleaner JSON (easier to read)
;;    - Better client-side handling (check presence, not null)
;;
;; 2. some? vs nil?
;;    `some?` returns true for any non-nil value (inverse of `nil?`)
;;      (some? nil) => false
;;      (some? false) => true
;;      (some? 0) => true
;;      (some? "") => true
;;    This correctly includes false, 0, empty string (unlike truthiness checks).
;;
;; 3. Threading with Conditional Updates
;;    Pattern:
;;      (-> base-map
;;          (assoc-some :field1 value1)
;;          (assoc-some :field2 value2))
;;    Each step conditionally adds a field. The thread makes it clear
;;    we're building up the response incrementally.
;;
;; 4. Required vs Optional Fields
;;    API design distinguishes:
;;    - Required: Always present (status, userId)
;;    - Optional: May be absent (errorMessage, data)
;;
;;    Required fields use `assoc` directly.
;;    Optional fields use `assoc-some`.
;;    This makes the contract explicit in code.
;;
;; 5. Keyword to String Conversion
;;    JSON has no keyword type, so :success → "success"
;;    Using `name` extracts the string: (name :success) => "success"
;;    This is lossy (can't distinguish :success vs "success") but
;;    acceptable for API boundaries.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Conditional field inclusion with assoc-some
;;
;; Real-world usage: The reference shows building JSON responses:
;;   (defn build-response [result]
;;     (-> {:status "success"}
;;         (assoc-some :data (:data result))
;;         (assoc-some :error (:error result))
;;         (assoc-some :warnings (:warnings result))))
;;
;; Production systems use this pattern for:
;; - REST API responses (omit null fields)
;; - GraphQL resolvers (conditional field resolution)
;; - Event payloads (include only relevant fields)
;; - Configuration files (omit defaults)
;;
;; Many libraries provide assoc-some (e.g., medley.core/assoc-some),
;; but implementing it is simple and instructive.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Success response with data
  (build-json-response
    {:status :success
     :user-id "U123"
     :success-message "Operation completed"
     :error-message nil
     :data {:result "OK"}
     :metadata nil})
  ;; => {:status "success"
  ;;     :userId "U123"
  ;;     :successMessage "Operation completed"
  ;;     :data {:result "OK"}}
  ;; Note: errorMessage and metadata omitted (nil)

  ;; Example 2: Error response with metadata
  (build-json-response
    {:status :error
     :user-id "U456"
     :success-message nil
     :error-message "Invalid input"
     :data nil
     :metadata {:timestamp "2024-01-15"}})
  ;; => {:status "error"
  ;;     :userId "U456"
  ;;     :errorMessage "Invalid input"
  ;;     :metadata {:timestamp "2024-01-15"}}

  ;; Example 3: Minimal response (only required fields)
  (build-json-response
    {:status :pending
     :user-id "U789"
     :success-message nil
     :error-message nil
     :data nil
     :metadata nil})
  ;; => {:status "pending"
  ;;     :userId "U789"}
  ;; All optional fields omitted

  ;; Example 4: All fields present
  (build-json-response
    {:status :success
     :user-id "U999"
     :success-message "Done"
     :error-message "Warning: deprecated field"
     :data {:count 10}
     :metadata {:version "1.0"}})
  ;; => {:status "success"
  ;;     :userId "U999"
  ;;     :successMessage "Done"
  ;;     :errorMessage "Warning: deprecated field"
  ;;     :data {:count 10}
  ;;     :metadata {:version "1.0"}}
)

;; TESTS
;; -----

(defn -test []
  ;; Test success response with data
  (let [result (build-json-response
                 {:status :success
                  :user-id "U123"
                  :success-message "Operation completed"
                  :error-message nil
                  :data {:result "OK"}
                  :metadata nil})]
    (assert (= (:status result) "success") "Should convert status keyword")
    (assert (= (:userId result) "U123") "Should rename user-id to userId")
    (assert (= (:successMessage result) "Operation completed") "Should include success message")
    (assert (not (contains? result :errorMessage)) "Should not include nil error message")
    (assert (= (:data result) {:result "OK"}) "Should include data")
    (assert (not (contains? result :metadata)) "Should not include nil metadata"))

  ;; Test error response with metadata
  (let [result (build-json-response
                 {:status :error
                  :user-id "U456"
                  :success-message nil
                  :error-message "Invalid input"
                  :data nil
                  :metadata {:timestamp "2024-01-15"}})]
    (assert (= (:status result) "error") "Should convert error status")
    (assert (= (:errorMessage result) "Invalid input") "Should include error message")
    (assert (not (contains? result :successMessage)) "Should not include nil success message")
    (assert (not (contains? result :data)) "Should not include nil data")
    (assert (= (:metadata result) {:timestamp "2024-01-15"}) "Should include metadata"))

  ;; Test minimal response (only required fields)
  (let [result (build-json-response
                 {:status :pending
                  :user-id "U789"
                  :success-message nil
                  :error-message nil
                  :data nil
                  :metadata nil})]
    (assert (= (:status result) "pending") "Should convert pending status")
    (assert (= (:userId result) "U789") "Should include userId")
    (assert (= (count result) 2) "Should only have 2 fields (status and userId)")
    (assert (not (contains? result :successMessage)) "Should not include nil fields")
    (assert (not (contains? result :errorMessage)) "Should not include nil fields")
    (assert (not (contains? result :data)) "Should not include nil fields")
    (assert (not (contains? result :metadata)) "Should not include nil fields"))

  ;; Test all fields present
  (let [result (build-json-response
                 {:status :success
                  :user-id "U999"
                  :success-message "Done"
                  :error-message "Warning: deprecated"
                  :data {:count 10}
                  :metadata {:version "1.0"}})]
    (assert (= (count result) 6) "Should include all 6 fields when non-nil")
    (assert (= (:successMessage result) "Done") "Should include success message")
    (assert (= (:errorMessage result) "Warning: deprecated") "Should include error message"))

  (println "✓ All tests passed! The build-json-response function works correctly."))

;; Run: (-test)
