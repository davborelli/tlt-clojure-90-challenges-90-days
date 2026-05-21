(ns parse-full-name
  (:require [clojure.string :as str]))

(defn parse-full-name
  [full-name]
  (let [names      (str/split full-name #"\s+")
        names-size (count names)]
    (cond
      (= names-size 1) {:first-name (first names) :last-name (first names)}
      (= names-size 2) {:first-name (first names) :last-name (last names)}
      :else {:first-name  (first names)
             :middle-name (str/join " " (rest (butlast names)))
             :last-name   (last names)}))) 
 

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
