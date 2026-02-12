(ns parse-full-name)

(defn parse-full-name
  [full-name]
  )

(defn- tst []
  (assert (=
(parse-full-name "John Michael Doe")
{:first-name "John" :middle-name "Michael" :last-name "Doe"}))

(assert (=
(parse-full-name "Jane Doe")
{:first-name "Jane" :last-name "Doe"}))

(assert (=
(parse-full-name "Madonna")
{:first-name "Madonna" :last-name "Madonna"}))

(assert (=
(parse-full-name "John Paul George Ringo Starr")
{:first-name "John" :middle-name "Paul George Ringo" :last-name "Starr"}))

  "SUCCESS")

(tst)
