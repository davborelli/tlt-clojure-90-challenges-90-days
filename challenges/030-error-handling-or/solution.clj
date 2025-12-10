;; =============================================================================
;; 030 - ERROR HANDLING WITH OR COMPOSITION
;; Level: 6/18 | Type: Controller
;; =============================================================================

(ns challenge-030.solution)

(defn fetch-with-fallback
  "Fetches user with multiple error checks and fallbacks.

  Parameters:
  - user-id: Integer user ID

  Returns: Error map or success map"
  [user-id]
  (or
    ;; Check 1: Invalid ID
    (when (<= user-id 0)
      {:status :error :message "Invalid ID"})

    ;; Check 2: Out of range
    (when (> user-id 1000)
      {:status :error :message "ID out of range"})

    ;; Check 3: User not found (odd IDs)
    (when (odd? user-id)
      {:status :error :message "User not found"})

    ;; Success case
    {:status :success
     :user {:id user-id
            :name (str "User " user-id)}}))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. or short-circuits: Returns first truthy value
;; 2. when returns nil if condition false, error if true
;; 3. Validation functions checked in order
;; 4. Success returned if all validations pass (return nil)

;; REFERENCE PATTERN
;; -----------------
;; Pattern inspired by: references/controllers/exemplo1.md
;; Pattern used: or composition for error handling

;; TESTS
;; -----

(defn -test []
  ;; Test success (even, in range, positive)
  (assert (= (fetch-with-fallback 100)
             {:status :success :user {:id 100 :name "User 100"}}))

  ;; Test invalid ID
  (assert (= (fetch-with-fallback 0)
             {:status :error :message "Invalid ID"}))
  (assert (= (fetch-with-fallback -5)
             {:status :error :message "Invalid ID"}))

  ;; Test out of range
  (assert (= (fetch-with-fallback 2000)
             {:status :error :message "ID out of range"}))

  ;; Test user not found (odd)
  (assert (= (fetch-with-fallback 51)
             {:status :error :message "User not found"}))
  (assert (= (fetch-with-fallback 1)
             {:status :error :message "User not found"}))

  ;; Test more success cases
  (assert (= (:status (fetch-with-fallback 2)) :success))
  (assert (= (:status (fetch-with-fallback 500)) :success))

  (println "✓ All tests passed!"))

;; Run: (-test)
