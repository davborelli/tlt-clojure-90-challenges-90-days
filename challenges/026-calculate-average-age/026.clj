(ns calculate-average-age)

(defn average-age
  [users])

(defn- tst []
  (assert (=
(average-age [{:name "John" :age 20} {:name "Jane" :age 30} {:name "Bob" :age 40}])
30.0))

(assert (=
(average-age [{:name "Alice" :age 25} {:name "Charlie" :age 35}])
30.0))

(assert (=
(average-age [])
0.0))

  "SUCCESS")

(tst)
