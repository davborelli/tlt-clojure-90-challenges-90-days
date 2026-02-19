(ns add-status-field)

(defn add-status
  [user]
  )

(defn- tst []
  (assert (=
(add-status {:name "John" :email "john@example.com"})
{:name "John" :email "john@example.com" :status :active}))

(assert (=
(add-status {:name "Jane" :email "jane@example.com"})
{:name "Jane" :email "jane@example.com" :status :active}))

(assert (=
(add-status {:name "Bob" :email "bob@test.com"})
{:name "Bob" :email "bob@test.com" :status :active}))

  "SUCCESS")

(tst)
