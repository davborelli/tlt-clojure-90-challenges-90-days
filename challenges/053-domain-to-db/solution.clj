;; =============================================================================
;; 053 - DOMAIN TO DATABASE (Part 1 of Bidirectional Pair)
;; Level: 11/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms domain entities into database records. The domain
;; model uses kebab-case keywords (idiomatic Clojure) and keyword enums, while
;; the database uses snake_case columns (SQL convention) and string values.
;;
;; We destructure the domain entity and construct the database record with
;; transformed keys and values. This is a common pattern in production systems:
;; separating domain representation from storage representation.
;;
;; This is Part 1 of a bidirectional transformation pair. Challenge 054
;; implements the reverse (database → domain). Together they enable round-trip
;; transformations essential for persistence layers.

(ns challenge-053.solution)

;; IMPLEMENTATION
;; --------------

(defn domain->db
  "Transforms domain user entity into database record.

  Transformations:
  - kebab-case keys → snake_case keys
  - :account-status keyword → string

  Parameters:
  - domain-user: Domain entity with kebab-case keys

  Returns: Database record with snake_case keys"
  [domain-user]
  (let [{:keys [user-id full-name email-address
                account-status created-at]} domain-user]
    {:user_id user-id
     :full_name full-name
     :email_address email-address
     ;; Convert keyword to string for database
     :account_status (name account-status)
     :created_at created-at}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Domain vs Database Representation
;;    Domain model (application-centric):
;;    - Uses idiomatic Clojure conventions
;;    - kebab-case keywords (:user-id, :full-name)
;;    - Keyword enums (:active, :suspended)
;;    - Nested structures when appropriate
;;
;;    Database schema (storage-centric):
;;    - Uses SQL/database conventions
;;    - snake_case columns (user_id, full_name)
;;    - String values for enums ("active", "suspended")
;;    - Flat rows (denormalized for queries)
;;
;;    Adapters bridge this impedance mismatch.
;;
;; 2. Bidirectional Transformations
;;    This challenge (053) implements domain → db.
;;    Challenge 054 implements db → domain.
;;    Together they form a bidirectional pair:
;;
;;      Domain Entity
;;           ↓ (053: domain->db)
;;      Database Record
;;           ↓ (054: db->domain)
;;      Domain Entity (round trip)
;;
;;    Essential for persistence: save and load data consistently.
;;
;; 3. Keyword to String Conversion
;;    Databases often store enums as strings:
;;      (name :active) => "active"
;;      (name :suspended) => "suspended"
;;    `name` extracts the string name from a keyword.
;;    Reverse uses `keyword`:
;;      (keyword "active") => :active
;;
;; 4. Case Convention Transformation
;;    Clojure: kebab-case (words-separated-by-hyphens)
;;    SQL: snake_case (words_separated_by_underscores)
;;    Python: snake_case
;;    JavaScript: camelCase
;;
;;    Adapters handle these conventions. In this case, we manually
;;    map each field. For dynamic transformation, libraries exist.
;;
;; 5. Separation of Concerns
;;    Domain layer doesn't know about database conventions.
;;    Database layer doesn't know about domain conventions.
;;    Adapter layer translates between them.
;;    This decoupling makes each layer simpler and more maintainable.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Bidirectional transformation (domain ↔ database)
;;
;; Real-world usage: Production systems transform between representations:
;;   ;; Saving to database
;;   (defn save-user! [domain-user]
;;     (let [db-record (domain->db domain-user)]
;;       (jdbc/insert! db {:table :users :data db-record})))
;;
;;   ;; Loading from database
;;   (defn load-user [user-id]
;;     (let [db-record (jdbc/query db ["SELECT * FROM users WHERE user_id = ?" user-id])
;;           domain-user (db->domain db-record)]
;;       domain-user))
;;
;; The reference shows similar bidirectional patterns essential for
;; persistence layers, API adapters, and external system integration.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Active user
  (domain->db
    {:user-id "USER-123"
     :full-name "Alice Johnson"
     :email-address "alice@example.com"
     :account-status :active
     :created-at "2023-01-15T10:00:00"})
  ;; => {:user_id "USER-123"
  ;;     :full_name "Alice Johnson"
  ;;     :email_address "alice@example.com"
  ;;     :account_status "active"
  ;;     :created_at "2023-01-15T10:00:00"}

  ;; Example 2: Suspended user
  (domain->db
    {:user-id "USER-456"
     :full-name "Bob Smith"
     :email-address "bob@example.com"
     :account-status :suspended
     :created-at "2024-01-01T08:30:00"})
  ;; => {:user_id "USER-456"
  ;;     :full_name "Bob Smith"
  ;;     :email_address "bob@example.com"
  ;;     :account_status "suspended"
  ;;     :created_at "2024-01-01T08:30:00"}

  ;; Example 3: Closed account
  (domain->db
    {:user-id "USER-789"
     :full-name "Charlie Brown"
     :email-address "charlie@example.com"
     :account-status :closed
     :created-at "2022-06-01T14:20:00"})
  ;; => {:account_status "closed"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test active user transformation
  (let [result (domain->db
                 {:user-id "USER-123"
                  :full-name "Alice Johnson"
                  :email-address "alice@example.com"
                  :account-status :active
                  :created-at "2023-01-15T10:00:00"})]
    (assert (= (:user_id result) "USER-123")
            "Should transform user-id to user_id")
    (assert (= (:full_name result) "Alice Johnson")
            "Should transform full-name to full_name")
    (assert (= (:email_address result) "alice@example.com")
            "Should transform email-address to email_address")
    (assert (= (:account_status result) "active")
            "Should convert :active keyword to string")
    (assert (= (:created_at result) "2023-01-15T10:00:00")
            "Should transform created-at to created_at"))

  ;; Test suspended user
  (let [result (domain->db
                 {:user-id "USER-456"
                  :full-name "Bob Smith"
                  :email-address "bob@example.com"
                  :account-status :suspended
                  :created-at "2024-01-01T08:30:00"})]
    (assert (= (:account_status result) "suspended")
            "Should convert :suspended keyword to string"))

  ;; Test closed account
  (let [result (domain->db
                 {:user-id "USER-789"
                  :full-name "Charlie Brown"
                  :email-address "charlie@example.com"
                  :account-status :closed
                  :created-at "2022-06-01T14:20:00"})]
    (assert (= (:account_status result) "closed")
            "Should convert :closed keyword to string"))

  (println "✓ All tests passed!"))

;; Run: (-test)
