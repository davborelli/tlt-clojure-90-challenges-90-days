(ns find-oldest-user)

(defn find-oldest
  [users]
  )

(defn- tst []
  (assert (=
(find-oldest [{:name "John" :age 25} {:name "Jane" :age 30} {:name "Bob" :age 20}])
{:name "Jane" :age 30}))

(assert (=
(find-oldest [{:name "Alice" :age 45} {:name "Charlie" :age 50}])
{:name "Charlie" :age 50}))

(assert (=
(find-oldest [{:name "Solo" :age 99}])
{:name "Solo" :age 99}))

  (let [result (find-oldest [{:name "Eve" :age 35} {:name "Frank" :age 35} {:name "Grace" :age 30}])]
    (assert (= (:age result) 35))
    (assert (contains? #{"Eve" "Frank"} (:name result))))

  "SUCCESS")

(tst)
