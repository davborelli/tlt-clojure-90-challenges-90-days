(ns parse-date-string)

(defn parse-date
  [date-string]
  )

(defn- tst []
  (assert (=
(parse-date "2024-01-15")
{:year 2024 :month 1 :day 15}))

(assert (=
(parse-date "1999-12-31")
{:year 1999 :month 12 :day 31}))

(assert (=
(parse-date "2000-06-01")
{:year 2000 :month 6 :day 1}))

  "SUCCESS")

(tst)
