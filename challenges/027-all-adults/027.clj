(ns all-adults)

;; (defn all-adults?
;;   [users]
;;   (every? #(>= (:age %) 18) users))

(defn all-adults?
  [users]
  (every? #(> (:age %) 18) users))

(defn- tst []
  (assert (=
(all-adults? [{:name "John" :age 25} {:name "Jane" :age 30}])
true))

(assert (=
(all-adults? [{:name "John" :age 25} {:name "Bob" :age 17}])
false))

(assert (=
(all-adults? [])
true))

  "SUCCESS")

(tst)
