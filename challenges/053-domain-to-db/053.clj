(ns domain-to-db)

(defn domain->db
  [domain-user]
  )

(defn- tst []
  (assert (=
(domain->db
  {:user-id "USER-123"
   :full-name "Alice Johnson"
   :email-address "alice@example.com"
   :account-status :active
   :created-at "2023-01-15T10:00:00"})
{:user_id "USER-123"
     :full_name "Alice Johnson"
     :email_address "alice@example.com"
     :account_status "active"
     :created_at "2023-01-15T10:00:00"}))

(assert (=
(domain->db
  {:user-id "USER-456"
   :full-name "Bob Smith"
   :email-address "bob@example.com"
   :account-status :suspended
   :created-at "2024-01-01T08:30:00"})
{:user_id "USER-456"
     :full_name "Bob Smith"
     :email_address "bob@example.com"
     :account_status "suspended"
     :created_at "2024-01-01T08:30:00"}))

  "SUCCESS")

(tst)
