;; =============================================================================
;; 052 - MULTI-SOURCE AGGREGATOR
;; Level: 11/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This solution demonstrates data aggregation from multiple sources using
;; merge. We combine user profile, preferences (with defaults for missing
;; values), and activity data into a single comprehensive user map. We also
;; calculate a derived field (activity-level) based on business rules.
;;
;; The approach uses merge to combine maps, with defaults provided for optional
;; preferences. We handle nil preferences gracefully using `or` to provide an
;; empty map. After merging, we calculate the activity level and add it to the
;; result.
;;
;; This pattern is common in microservices architectures where different services
;; own different aspects of user data, and aggregation layers combine them into
;; unified views for API responses or UI rendering.

(ns challenge-052.solution)

;; IMPLEMENTATION
;; --------------

(defn calculate-activity-level
  "Calculates activity level based on login count.

  Parameters:
  - login-count: Number of user logins

  Returns: Activity level keyword"
  [login-count]
  (cond
    (>= login-count 100) :very-active
    (>= login-count 50)  :active
    (>= login-count 10)  :moderate
    :else                :low))

(defn aggregate-user-data
  "Aggregates user data from multiple sources with defaults and calculations.

  Parameters:
  - user-profile: Basic user information (required)
  - user-preferences: User settings (may be nil)
  - user-activity: Activity metrics (required)

  Returns: Merged user data with calculated activity-level"
  [user-profile user-preferences user-activity]
  (let [;; Define defaults for missing preferences
        defaults {:theme "default"
                  :language "en"
                  :notifications false}
        ;; Handle nil preferences (replace with empty map)
        prefs (or user-preferences {})
        ;; Merge: defaults first, then sources (later values override)
        merged (merge defaults user-profile prefs user-activity)
        ;; Calculate activity level from login count
        activity-level (calculate-activity-level (:login-count merged))]
    ;; Add calculated field to result
    (assoc merged :activity-level activity-level)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Data Aggregation with merge
;;    merge combines multiple maps into one:
;;      (merge {:a 1} {:b 2} {:c 3}) => {:a 1 :b 2 :c 3}
;;    When keys overlap, later maps override earlier ones:
;;      (merge {:a 1} {:a 2}) => {:a 2}
;;    This makes merge perfect for combining data sources with priority.
;;
;; 2. Defaults Pattern
;;    Provide defaults by merging defaults first:
;;      (merge {:theme "default"} preferences)
;;    If preferences has :theme, it overrides default.
;;    If preferences doesn't have :theme, default remains.
;;    This ensures required fields always have values.
;;
;; 3. Handling nil with or
;;    User preferences might be nil (not yet set). Use `or`:
;;      (or user-preferences {})
;;    If preferences is nil, use empty map.
;;    If preferences exists, use it.
;;    This prevents nil from breaking merge.
;;
;; 4. Calculated/Derived Fields
;;    Aggregation often includes calculated fields:
;;    - activity-level (derived from login-count)
;;    - total-score (sum of multiple scores)
;;    - risk-rating (based on multiple factors)
;;    Calculate after merging, when all data is available.
;;
;; 5. Multi-Source Architecture
;;    In microservices:
;;    - User service → profile data
;;    - Settings service → preferences
;;    - Analytics service → activity data
;;    Aggregation layers combine them for complete views.
;;    This pattern decouples services while providing unified data.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo1.md
;;
;; Pattern used: Data aggregation from multiple sources
;;
;; Real-world usage: Production systems aggregate data from multiple services:
;;   (defn build-user-view [user-id]
;;     (let [profile (fetch-profile user-id)
;;           settings (fetch-settings user-id)
;;           activity (fetch-activity user-id)]
;;       (merge default-settings profile settings activity)))
;;
;; This demonstrates how production code combines data from different sources
;; to build comprehensive views, handling missing data with defaults and
;; calculating derived fields for business logic.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Complete data (all sources present)
  (aggregate-user-data
    {:user-id "USER-1" :name "Alice" :email "alice@example.com" :join-date "2023-01-15"}
    {:theme "dark" :language "pt" :notifications true}
    {:last-login "2024-01-15" :login-count 150 :posts-count 45})
  ;; => {:user-id "USER-1"
  ;;     :name "Alice"
  ;;     :email "alice@example.com"
  ;;     :join-date "2023-01-15"
  ;;     :theme "dark"
  ;;     :language "pt"
  ;;     :notifications true
  ;;     :last-login "2024-01-15"
  ;;     :login-count 150
  ;;     :posts-count 45
  ;;     :activity-level :very-active}

  ;; Example 2: Missing preferences (nil)
  (aggregate-user-data
    {:user-id "USER-2" :name "Bob" :email "bob@example.com" :join-date "2024-01-01"}
    nil
    {:last-login "2024-01-10" :login-count 5 :posts-count 2})
  ;; => {:user-id "USER-2"
  ;;     :name "Bob"
  ;;     :email "bob@example.com"
  ;;     :join-date "2024-01-01"
  ;;     :theme "default"
  ;;     :language "en"
  ;;     :notifications false
  ;;     :last-login "2024-01-10"
  ;;     :login-count 5
  ;;     :posts-count 2
  ;;     :activity-level :low}

  ;; Example 3: Partial preferences
  (aggregate-user-data
    {:user-id "USER-3" :name "Charlie" :email "charlie@example.com" :join-date "2023-06-01"}
    {:theme "light"}  ; Only theme, no language or notifications
    {:last-login "2024-01-14" :login-count 75 :posts-count 30})
  ;; => {:theme "light"  ; provided
  ;;     :language "en"  ; default
  ;;     :notifications false  ; default
  ;;     :activity-level :active}  ; 75 logins
)

;; TESTS
;; -----

(defn -test []
  ;; Test complete data
  (let [result (aggregate-user-data
                 {:user-id "USER-1" :name "Alice" :email "alice@example.com" :join-date "2023-01-15"}
                 {:theme "dark" :language "pt" :notifications true}
                 {:last-login "2024-01-15" :login-count 150 :posts-count 45})]
    (assert (= (:user-id result) "USER-1")
            "Should include profile data")
    (assert (= (:theme result) "dark")
            "Should include preference data")
    (assert (= (:login-count result) 150)
            "Should include activity data")
    (assert (= (:activity-level result) :very-active)
            "Should calculate activity level (>=100)"))

  ;; Test nil preferences
  (let [result (aggregate-user-data
                 {:user-id "USER-2" :name "Bob" :email "bob@example.com" :join-date "2024-01-01"}
                 nil
                 {:last-login "2024-01-10" :login-count 5 :posts-count 2})]
    (assert (= (:theme result) "default")
            "Should use default theme when preferences nil")
    (assert (= (:language result) "en")
            "Should use default language when preferences nil")
    (assert (false? (:notifications result))
            "Should use default notifications when preferences nil")
    (assert (= (:activity-level result) :low)
            "Should calculate low activity (<10)"))

  ;; Test partial preferences
  (let [result (aggregate-user-data
                 {:user-id "USER-3" :name "Charlie" :email "charlie@example.com" :join-date "2023-06-01"}
                 {:theme "light"}
                 {:last-login "2024-01-14" :login-count 75 :posts-count 30})]
    (assert (= (:theme result) "light")
            "Should use provided theme")
    (assert (= (:language result) "en")
            "Should use default for missing language")
    (assert (= (:activity-level result) :active)
            "Should calculate active level (>=50, <100)"))

  ;; Test activity level boundaries
  (assert (= (calculate-activity-level 100) :very-active)
          "Should be very-active at exactly 100")
  (assert (= (calculate-activity-level 50) :active)
          "Should be active at exactly 50")
  (assert (= (calculate-activity-level 10) :moderate)
          "Should be moderate at exactly 10")
  (assert (= (calculate-activity-level 9) :low)
          "Should be low below 10")

  (println "✓ All tests passed!"))

;; Run: (-test)
