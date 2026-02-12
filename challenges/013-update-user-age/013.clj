(ns update-user-age)

(defn birthday
  [user]
  )

(defn- tst []
  (assert (=
(birthday {:name "John" :age 25})
{:name "John" :age 26}))

(assert (=
(birthday {:name "Jane" :age 17})
{:name "Jane" :age 18}))

(assert (=
(birthday {:name "Bob" :age 0})
{:name "Bob" :age 1}))

(assert (=
(birthday {:name "Alice" :age 99})
{:name "Alice" :age 100}))

  "SUCCESS")

(tst)
