(ns bidirectional-transform)

(defn domain->wire
  [domain-data]
  )

(defn wire->domain
  [wire-data]
  )

(defn- tst []
  (assert (=
(domain->wire {:user-id 123 :full-name "John" :email-address "j@test.com"})
{"userId" 123 "fullName" "John" "emailAddress" "j@test.com"}))

(assert (=
(wire->domain {"userId" 456 "fullName" "Jane" "emailAddress" "jane@test.com"})
{:user-id 456 :full-name "Jane" :email-address "jane@test.com"}))

  "SUCCESS")

(tst)
