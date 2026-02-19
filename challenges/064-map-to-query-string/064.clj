(ns map-to-query-string
  (:require [clojure.string :as str]))

(defn map->query-string
  [params-map]
  )

(defn- tst []
  (let [result1 (map->query-string {:name "John" :age "25" :city "NYC"})]
    (assert (str/includes? result1 "name=John"))
    (assert (str/includes? result1 "age=25"))
    (assert (str/includes? result1 "city=NYC"))
    (assert (= 2 (count (re-seq #"&" result1)))))

  (let [result2 (map->query-string {:status "active" :verified "true"})]
    (assert (str/includes? result2 "status=active"))
    (assert (str/includes? result2 "verified=true"))
    (assert (= 1 (count (re-seq #"&" result2)))))

  (assert (=
(map->query-string {})
""))

  "SUCCESS")

(tst)
