(ns parse-date-string
  (:require [clojure.string :as str]))

(defn parse-date
  [date-string]
  (let [[year month day] (str/split date-string #"-")]
    {:year (parse-long year)
     :month (parse-long month)
     :day (parse-long day)}))

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
