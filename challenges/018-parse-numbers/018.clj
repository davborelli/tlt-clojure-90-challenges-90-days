(ns parse-numbers)

;; (defn parse-numbers
;;   [user-data]
;;   (-> user-data
;;       (update :age #(Integer/parseInt %))
;;       (update :score #(Integer/parseInt %))))

;; Versão mais idiomática
(defn parse-numbers
  [user-data]
  (reduce #(update %1 %2 (fn [v] (Integer/parseInt v)))
          user-data
          [:age :score]))

(defn- tst []
  (assert (=
(parse-numbers {:name "John" :age "25" :score "100"})
{:name "John" :age 25 :score 100}))

(assert (=
(parse-numbers {:name "Jane" :age "30" :score "95"})
{:name "Jane" :age 30 :score 95}))

(assert (=
(parse-numbers {:name "Bob" :age "18" :score "0"})
{:name "Bob" :age 18 :score 0}))

  "SUCCESS")

(tst)
