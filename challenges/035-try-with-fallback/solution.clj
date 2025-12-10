;; =============================================================================
;; 035 - TRY OPERATION WITH FALLBACK
;; Level: 7/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This controller implements a fallback pattern using or composition.
;; It tries to fetch from a primary source first, then falls back to a
;; secondary source, and finally returns an error if both fail.
;;
;; The or operator evaluates expressions left to right and returns the first
;; truthy value, making it perfect for fallback logic. Each fetch function
;; returns either a success map (truthy) or nil (falsy), allowing or to
;; automatically select the first successful result.
;;
;; This pattern is common in production systems that need resilience:
;; trying cache before database, trying multiple API endpoints, or using
;; default values when preferred sources are unavailable.

(ns challenge-035.solution)

;; IMPLEMENTATION
;; --------------

(defn try-fetch-primary
  "Attempts to fetch user from primary source (succeeds if even and <= 100)."
  [user-id]
  (when (and (even? user-id) (<= user-id 100))
    {:status :success
     :source :primary
     :user {:id user-id
            :name (str "User " user-id)}}))

(defn try-fetch-secondary
  "Attempts to fetch user from secondary source (succeeds if even)."
  [user-id]
  (when (even? user-id)
    {:status :success
     :source :secondary
     :user {:id user-id
            :name (str "User " user-id)}}))

(defn try-fetch-with-fallback
  "Tries to fetch user from primary, then secondary, then returns error.

  Parameters:
  - user-id: The user ID to fetch

  Returns: Map with status and either source/user or error message"
  [user-id]
  (or
    ;; Try primary source first (even AND <= 100)
    (try-fetch-primary user-id)
    ;; If primary fails, try secondary (even, any value)
    (try-fetch-secondary user-id)
    ;; If both fail, return error
    {:status :error
     :message "User not found in any source"}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. or Composition for Fallback
;;    The or operator evaluates expressions left to right and returns the
;;    first truthy value (anything except nil or false). This makes it perfect
;;    for implementing fallback logic: try option A, if that fails (returns nil),
;;    try option B, if that fails, use default C.
;;
;; 2. when Returns nil on False
;;    The when macro returns nil if its condition is false, and the body result
;;    if true. This makes it work perfectly with or: when condition fails,
;;    when returns nil, and or tries the next expression.
;;
;; 3. Helper Functions for Clarity
;;    Extracting try-fetch-primary and try-fetch-secondary as separate functions
;;    makes the code more testable and the fallback logic more readable.
;;    Each function has a single responsibility and clear success conditions.
;;
;; 4. Truthy and Falsy Values
;;    In Clojure, only nil and false are falsy; everything else is truthy.
;;    Empty collections [], empty strings "", and 0 are all truthy.
;;    This is why we return nil from when (not false) - both work with or.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo1.md
;;
;; Pattern used: or composition for error handling and fallback
;;
;; Real-world usage: The reference code shows this pattern:
;;   (or (db/fetch-avatar-by-id! deps avatar-id)
;;       (ex/not-found!))
;;
;;   (or (some-> (db/fetch-avatar-by-customer-id! deps customer-id)
;;               (first))
;;       (ex/not-found!))
;;
;; This demonstrates how production controllers use or to attempt operations
;; and fall back to error handling (throwing exceptions or returning errors)
;; when operations fail. The pattern ensures graceful degradation.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Primary succeeds (even and <= 100)
  (try-fetch-with-fallback 50)
  ;; => {:status :success, :source :primary, :user {:id 50, :name "User 50"}}

  ;; Example 2: Secondary succeeds (even but > 100)
  (try-fetch-with-fallback 150)
  ;; => {:status :success, :source :secondary, :user {:id 150, :name "User 150"}}

  ;; Example 3: Both fail (odd number)
  (try-fetch-with-fallback 99)
  ;; => {:status :error, :message "User not found in any source"}

  ;; Example 4: Primary succeeds (boundary case)
  (try-fetch-with-fallback 100)
  ;; => {:status :success, :source :primary, :user {:id 100, :name "User 100"}}

  ;; Example 5: Secondary succeeds (boundary case)
  (try-fetch-with-fallback 102)
  ;; => {:status :success, :source :secondary, :user {:id 102, :name "User 102"}}
)

;; TESTS
;; -----

(defn -test []
  ;; Test primary source success
  (assert (= (try-fetch-with-fallback 50)
             {:status :success :source :primary :user {:id 50 :name "User 50"}})
          "Should fetch from primary for even <= 100")

  ;; Test secondary source success
  (assert (= (try-fetch-with-fallback 150)
             {:status :success :source :secondary :user {:id 150 :name "User 150"}})
          "Should fetch from secondary for even > 100")

  ;; Test both sources fail
  (assert (= (try-fetch-with-fallback 99)
             {:status :error :message "User not found in any source"})
          "Should return error for odd numbers")

  ;; Test boundary cases
  (assert (= (:source (try-fetch-with-fallback 100)) :primary)
          "Should use primary for 100 (boundary)")
  (assert (= (:source (try-fetch-with-fallback 102)) :secondary)
          "Should use secondary for 102 (just above boundary)")
  (assert (= (try-fetch-with-fallback 1)
             {:status :error :message "User not found in any source"})
          "Should return error for odd 1")

  (println "✓ All tests passed!"))

;; Run: (-test)
