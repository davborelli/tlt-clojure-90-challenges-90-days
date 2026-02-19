(ns extract-user-fields)

(defn extract-contact-info
  [user]
  )

(defn- tst []
  (assert (=
(extract-contact-info {:name "John Doe" :email "john@example.com" :age 30 :address "123 Main St" :phone "555-1234"})
{:name "John Doe" :email "john@example.com"}))

(assert (=
(extract-contact-info {:name "Jane Smith" :email "jane@example.com" :age 25 :address "456 Oak Ave" :phone "555-5678"})
{:name "Jane Smith" :email "jane@example.com"}))

(assert (=
(extract-contact-info {:name "Bob Johnson" :email "bob@test.com" :age 40 :address "789 Pine Rd" :phone "555-9999"})
{:name "Bob Johnson" :email "bob@test.com"}))

  "SUCCESS")

(tst)
