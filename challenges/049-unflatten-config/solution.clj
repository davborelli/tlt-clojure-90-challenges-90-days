;; =============================================================================
;; 049 - UNFLATTEN CONFIG
;; Level: 10/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter performs the reverse of flattening: it builds a nested
;; structure from flat key-value pairs. We destructure the flat config to
;; extract all values, then construct a nested map organized by categories.
;;
;; The approach uses direct map construction rather than assoc-in because we
;; know the structure ahead of time. This is more concise and readable than
;; threading multiple assoc-in calls.
;;
;; This pattern is common when loading configuration from environment variables
;; or flat property files. The flat format is convenient for storage/deployment,
;; but nested structures are easier to work with in application code (better
;; namespacing, organization, and access patterns).

(ns challenge-049.solution)

;; IMPLEMENTATION
;; --------------

(defn unflatten-config
  "Transforms flat config into nested structure organized by category.

  Parameters:
  - flat-config: Flat map with prefixed keys (:db-*, :api-*, :log-*)

  Returns: Nested map with :database, :api, :logging categories"
  [flat-config]
  ;; Destructure all flat keys
  (let [{:keys [db-host db-port db-name
                api-base-url api-timeout api-retry-count
                log-level log-file]} flat-config]
    ;; Build nested structure directly
    {:database {:host db-host
                :port db-port
                :name db-name}
     :api {:base-url api-base-url
           :timeout api-timeout
           :retry-count api-retry-count}
     :logging {:level log-level
               :file log-file}}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Reverse Transformation: Flat → Nested
;;    This is the opposite of flattening (challenge 048).
;;    Flat format:
;;      {:db-host "..." :db-port ... :api-base-url "..."}
;;    Nested format:
;;      {:database {:host "..." :port ...} :api {:base-url "..."}}
;;    Why build nested from flat?
;;    - Better organization (grouped by category)
;;    - Easier to access related config (all db config in one place)
;;    - More maintainable (clear structure)
;;
;; 2. Direct Construction vs assoc-in
;;    We could use assoc-in:
;;      (-> {}
;;          (assoc-in [:database :host] db-host)
;;          (assoc-in [:database :port] db-port)
;;          ...)
;;    But direct construction is more concise when structure is known:
;;      {:database {:host db-host :port db-port ...}
;;       :api {...}
;;       :logging {...}}
;;    Use direct construction when possible, assoc-in when building dynamically.
;;
;; 3. Prefix Naming Convention
;;    Flat configs use prefixes to indicate grouping:
;;    - db-* for database config
;;    - api-* for API config
;;    - log-* for logging config
;;    The nested structure captures this grouping explicitly.
;;    In nested version, we drop prefixes (db-host → host in :database)
;;
;; 4. Configuration Management Patterns
;;    Real systems often:
;;    - Load flat config from env vars (DB_HOST, API_TIMEOUT, etc.)
;;    - Transform to nested for application use
;;    - Access as (get-in config [:database :host])
;;    This gives clean separation between deployment concerns (flat env vars)
;;    and application concerns (nested config structure).
;;
;; 5. Namespacing Through Nesting
;;    Nesting provides namespacing:
;;    - :database/:host vs :api/:base-url (both could use :url key)
;;    - :api/:timeout vs :database/:timeout (different timeouts)
;;    Without nesting, you'd need fully qualified keys everywhere.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Building nested structures (reverse of flatten)
;;
;; Real-world usage: Configuration loading in production systems:
;;   (defn load-config []
;;     (let [env (System/getenv)]
;;       {:database {:host (get env "DB_HOST")
;;                   :port (Integer/parseInt (get env "DB_PORT"))}
;;        :api {...}
;;        :logging {...}}))
;;
;; This demonstrates how adapters transform flat deployment config into
;; nested application config, separating concerns and improving code quality.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Development configuration
  (unflatten-config
    {:db-host "localhost"
     :db-port 5432
     :db-name "myapp"
     :api-base-url "https://api.example.com"
     :api-timeout 30
     :api-retry-count 3
     :log-level "info"
     :log-file "/var/log/app.log"})
  ;; => {:database {:host "localhost" :port 5432 :name "myapp"}
  ;;     :api {:base-url "https://api.example.com" :timeout 30 :retry-count 3}
  ;;     :logging {:level "info" :file "/var/log/app.log"}}

  ;; Example 2: Production configuration
  (unflatten-config
    {:db-host "db.example.com"
     :db-port 3306
     :db-name "production"
     :api-base-url "https://prod-api.example.com"
     :api-timeout 60
     :api-retry-count 5
     :log-level "warn"
     :log-file "/var/log/prod.log"})
  ;; => {:database {:host "db.example.com" :port 3306 :name "production"}
  ;;     :api {:base-url "https://prod-api.example.com" :timeout 60 :retry-count 5}
  ;;     :logging {:level "warn" :file "/var/log/prod.log"}}

  ;; Example 3: Accessing nested config
  (let [config (unflatten-config {...})]
    (get-in config [:database :host])     ;; => "localhost"
    (get-in config [:api :timeout])       ;; => 30
    (get-in config [:logging :level]))    ;; => "info"
)

;; TESTS
;; -----

(defn -test []
  (let [result (unflatten-config
                 {:db-host "localhost"
                  :db-port 5432
                  :db-name "myapp"
                  :api-base-url "https://api.example.com"
                  :api-timeout 30
                  :api-retry-count 3
                  :log-level "info"
                  :log-file "/var/log/app.log"})]
    ;; Test database nesting
    (assert (= (get-in result [:database :host]) "localhost")
            "Should nest db-host under :database")
    (assert (= (get-in result [:database :port]) 5432)
            "Should nest db-port under :database")
    (assert (= (get-in result [:database :name]) "myapp")
            "Should nest db-name under :database")
    ;; Test API nesting
    (assert (= (get-in result [:api :base-url]) "https://api.example.com")
            "Should nest api-base-url under :api")
    (assert (= (get-in result [:api :timeout]) 30)
            "Should nest api-timeout under :api")
    (assert (= (get-in result [:api :retry-count]) 3)
            "Should nest api-retry-count under :api")
    ;; Test logging nesting
    (assert (= (get-in result [:logging :level]) "info")
            "Should nest log-level under :logging")
    (assert (= (get-in result [:logging :file]) "/var/log/app.log")
            "Should nest log-file under :logging"))

  ;; Test production config
  (let [result (unflatten-config
                 {:db-host "db.example.com"
                  :db-port 3306
                  :db-name "production"
                  :api-base-url "https://prod-api.example.com"
                  :api-timeout 60
                  :api-retry-count 5
                  :log-level "warn"
                  :log-file "/var/log/prod.log"})]
    (assert (= (get-in result [:database :host]) "db.example.com")
            "Should handle production db host")
    (assert (= (get-in result [:database :port]) 3306)
            "Should handle different port")
    (assert (= (get-in result [:api :timeout]) 60)
            "Should handle different timeout")
    (assert (= (get-in result [:logging :level]) "warn")
            "Should handle different log level"))

  (println "✓ All tests passed!"))

;; Run: (-test)
