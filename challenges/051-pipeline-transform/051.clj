(ns pipeline-transform)

(defn process-registration
  [user-data]
  )

(defn- tst []
  (assert (=
(process-registration
  {:name "  Alice Johnson  "
   :email "  ALICE@EXAMPLE.COM  "
   :age "25"})
{:name "Alice Johnson"
 :email "alice@example.com"
 :age 25
 :registered-at "2024-01-15T10:00:00"}))

(assert (=
(process-registration
  {:name "Bob Smith"
   :email "BOB@EXAMPLE.COM"
   :age "30"})
{:name "Bob Smith"
 :email "bob@example.com"
 :age 30
 :registered-at "2024-01-15T10:00:00"}))

  "SUCCESS")

(tst)
