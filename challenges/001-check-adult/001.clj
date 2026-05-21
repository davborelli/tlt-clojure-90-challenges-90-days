(ns check-adult)

(defn adult?
  [age]
  (>= age 18))

(defn- tst []
  (assert (= (adult? 18) true))
  (assert (= (adult? 17) false))
  (assert (= (adult? 25) true))
  (assert (= (adult? 0) false))
  "SUCCESS")

(tst)
