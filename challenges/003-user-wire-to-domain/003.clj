(ns user-wire-to-domain)

(defn wire->domain
  [wire-user]
  )

(defn- tst []
  (assert (=
(wire->domain {:firstName "John" :lastName "Doe" :emailAddress "john@example.com"})
{:first-name "John" :last-name "Doe" :email "john@example.com"}))

(assert (=
(wire->domain {:firstName "Jane" :lastName "Smith" :emailAddress "jane@example.com"})
{:first-name "Jane" :last-name "Smith" :email "jane@example.com"}))

(assert (=
(wire->domain {:firstName "Bob" :lastName "Johnson" :emailAddress "bob@test.com"})
{:first-name "Bob" :last-name "Johnson" :email "bob@test.com"}))

  "SUCCESS")

(tst)
