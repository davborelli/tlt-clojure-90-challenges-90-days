(ns remove-sensitive-fields)

(defn remove-sensitive
  [user]
  )

(defn- tst []
  (assert (=
(remove-sensitive {:name "John" :email "john@example.com" :password "secret123" :ssn "123-45-6789" :age 30})
{:name "John" :email "john@example.com" :age 30}))

(assert (=
(remove-sensitive {:name "Jane" :email "jane@example.com" :password "pass456" :ssn "987-65-4321" :age 25})
{:name "Jane" :email "jane@example.com" :age 25}))

(assert (=
(remove-sensitive {:name "Bob" :email "bob@test.com" :password "mypass" :ssn "111-22-3333" :age 40})
{:name "Bob" :email "bob@test.com" :age 40}))

  "SUCCESS")

(tst)
