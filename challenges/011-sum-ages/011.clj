(ns sum-ages)

(defn sum-ages
  [users]
  )

(defn- tst []
  (assert (=
(sum-ages [{:name "John" :age 25} {:name "Jane" :age 30} {:name "Bob" :age 45}])
100))

(assert (=
(sum-ages [{:name "Alice" :age 18}])
18))

(assert (=
(sum-ages [])
0))

(assert (=
(sum-ages [{:name "Diana" :age 20} {:name "Eve" :age 22} {:name "Frank" :age 28}])
70))

  "SUCCESS")

(tst)
