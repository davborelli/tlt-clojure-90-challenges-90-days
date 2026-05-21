(ns filter-adults)

(defn is-legal-age?
  "Check if the person is of legal"
  [age]
  (>= age 18))

;; (defn filter-adults
;;   [users]
;;   (filter (comp is-legal-age? :age) users))

(defn filter-adults
  [users]
  (filter #(is-legal-age? (:age %)) users))

(defn- tst []
  (assert (=
(filter-adults [{:name "John" :age 25} {:name "Jane" :age 17} {:name "Bob" :age 30}])
[{:name "John" :age 25} {:name "Bob" :age 30}]))

(assert (=
(filter-adults [{:name "Alice" :age 16} {:name "Charlie" :age 15}])
[]))

(assert (=
(filter-adults [{:name "Diana" :age 18} {:name "Eve" :age 18}])
[{:name "Diana" :age 18} {:name "Eve" :age 18}]))

(assert (=
(filter-adults [])
[]))

  "SUCCESS")

(tst)
