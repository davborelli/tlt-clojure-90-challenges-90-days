(ns add-full-name)

(defn add-full-name
  [user]
  )

(defn- tst []
  (assert (=
(add-full-name {:first-name "John" :last-name "Doe"})
{:first-name "John" :last-name "Doe" :full-name "John Doe"}))

(assert (=
(add-full-name {:first-name "Jane" :last-name "Smith"})
{:first-name "Jane" :last-name "Smith" :full-name "Jane Smith"}))

(assert (=
(add-full-name {:first-name "Bob" :last-name "Johnson"})
{:first-name "Bob" :last-name "Johnson" :full-name "Bob Johnson"}))

  "SUCCESS")

(tst)
