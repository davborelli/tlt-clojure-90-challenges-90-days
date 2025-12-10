;; =============================================================================
;; 054 - DATABASE TO DOMAIN (Part 2 of Bidirectional Pair)
;; Level: 11/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms database records back into domain entities. It's
;; the inverse of Challenge 053: we convert snake_case back to kebab-case,
;; and string values back to keyword enums.
;;
;; This transformation is essential when loading data from the database. The
;; database stores data in its own format (SQL conventions), but the application
;; works with domain entities (Clojure conventions).
;;
;; Together with Challenge 053, this forms a complete bidirectional transformation
;; enabling round-trip persistence: save domain entities to database and load
;; them back without data loss or format corruption.

(ns challenge-054.solution)

;; IMPLEMENTATION
;; --------------

(defn db->domain
  "Transforms database record into domain user entity.

  Transformations:
  - snake_case keys → kebab-case keys
  - account_status string → keyword

  Parameters:
  - db-record: Database record with snake_case keys

  Returns: Domain entity with kebab-case keys"
  [db-record]
  (let [{:keys [user_id full_name email_address
                account_status created_at]} db-record]
    {:user-id user_id
     :full-name full_name
     :email-address email_address
     ;; Convert string to keyword for domain
     :account-status (keyword account_status)
     :created-at created_at}))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Inverse Transformation
;;    This is the exact inverse of Challenge 053 (domain->db):
;;
;;    Challenge 053 (domain->db):
;;      kebab-case → snake_case
;;      keyword → string
;;
;;    Challenge 054 (db->domain):
;;      snake_case → kebab-case
;;      string → keyword
;;
;;    Inverse transformations enable round trips:
;;      entity == (db->domain (domain->db entity))
;;
;; 2. Round-Trip Property
;;    Bidirectional transformations should preserve data:
;;      (= original (db->domain (domain->db original)))
;;    This is called the round-trip property. It ensures:
;;    - No data is lost in transformation
;;    - No data is corrupted
;;    - Save and load operations are consistent
;;    Essential for persistence layers.
;;
;; 3. String to Keyword Conversion
;;    Databases store enums as strings, domain uses keywords:
;;      (keyword "active") => :active
;;      (keyword "suspended") => :suspended
;;    `keyword` creates a keyword from a string.
;;    This is the inverse of `name` (keyword → string).
;;
;; 4. Destructuring with snake_case
;;    Clojure allows any valid keyword in destructuring:
;;      {:keys [user_id full_name]}
;;    Even though snake_case isn't idiomatic Clojure, it works fine.
;;    We use it here to destructure database records, then transform
;;    to kebab-case in the output.
;;
;; 5. Persistence Layer Pattern
;;    Production systems use these transformations in persistence layers:
;;
;;    Saving:
;;      domain entity → (domain->db) → database record → SQL INSERT
;;
;;    Loading:
;;      SQL SELECT → database record → (db->domain) → domain entity
;;
;;    This decouples domain logic from storage details.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo5.md
;;
;; Pattern used: Bidirectional transformation (database → domain)
;;
;; Real-world usage: Production persistence layers transform on load:
;;   (defn find-user-by-id [user-id]
;;     (let [db-record (jdbc/query db ["SELECT * FROM users WHERE user_id = ?" user-id])
;;           domain-user (db->domain (first db-record))]
;;       domain-user))
;;
;;   (defn update-user! [user-id updates]
;;     (let [domain-user (find-user-by-id user-id)
;;           updated (merge domain-user updates)
;;           db-record (domain->db updated)]
;;       (jdbc/update! db :users db-record ["user_id = ?" user-id])))
;;
;; The reference shows how bidirectional transformations are essential for
;; clean separation between domain models and database schemas.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Active user from database
  (db->domain
    {:user_id "USER-123"
     :full_name "Alice Johnson"
     :email_address "alice@example.com"
     :account_status "active"
     :created_at "2023-01-15T10:00:00"})
  ;; => {:user-id "USER-123"
  ;;     :full-name "Alice Johnson"
  ;;     :email-address "alice@example.com"
  ;;     :account-status :active
  ;;     :created-at "2023-01-15T10:00:00"}

  ;; Example 2: Suspended user from database
  (db->domain
    {:user_id "USER-456"
     :full_name "Bob Smith"
     :email_address "bob@example.com"
     :account_status "suspended"
     :created_at "2024-01-01T08:30:00"})
  ;; => {:user-id "USER-456"
  ;;     :full-name "Bob Smith"
  ;;     :email-address "bob@example.com"
  ;;     :account-status :suspended
  ;;     :created-at "2024-01-01T08:30:00"}

  ;; Example 3: Round-trip test (requires domain->db from challenge 053)
  ;; (require '[challenge-053.solution :refer [domain->db]])
  ;;
  ;; (def original {:user-id "USER-123"
  ;;                :full-name "Alice"
  ;;                :email-address "alice@example.com"
  ;;                :account-status :active
  ;;                :created-at "2023-01-15T10:00:00"})
  ;;
  ;; (= original (-> original domain->db db->domain))
  ;; ;; => true (round-trip preserves data)
)

;; TESTS
;; -----

(defn -test []
  ;; Test active user transformation
  (let [result (db->domain
                 {:user_id "USER-123"
                  :full_name "Alice Johnson"
                  :email_address "alice@example.com"
                  :account_status "active"
                  :created_at "2023-01-15T10:00:00"})]
    (assert (= (:user-id result) "USER-123")
            "Should transform user_id to user-id")
    (assert (= (:full-name result) "Alice Johnson")
            "Should transform full_name to full-name")
    (assert (= (:email-address result) "alice@example.com")
            "Should transform email_address to email-address")
    (assert (= (:account-status result) :active)
            "Should convert 'active' string to keyword")
    (assert (= (:created-at result) "2023-01-15T10:00:00")
            "Should transform created_at to created-at"))

  ;; Test suspended user
  (let [result (db->domain
                 {:user_id "USER-456"
                  :full_name "Bob Smith"
                  :email_address "bob@example.com"
                  :account_status "suspended"
                  :created_at "2024-01-01T08:30:00"})]
    (assert (= (:account-status result) :suspended)
            "Should convert 'suspended' string to keyword"))

  ;; Test closed account
  (let [result (db->domain
                 {:user_id "USER-789"
                  :full_name "Charlie Brown"
                  :email_address "charlie@example.com"
                  :account_status "closed"
                  :created_at "2022-06-01T14:20:00"})]
    (assert (= (:account-status result) :closed)
            "Should convert 'closed' string to keyword"))

  (println "✓ All tests passed!"))

;; Run: (-test)
