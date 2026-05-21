(ns update-nested-field)

(defn increment-age
  [user-data]
  (update-in user-data [:user :details :age] inc))

(defn- tst []
  (assert (=
(increment-age {:user {:name "John" :details {:age 25}}})
{:user {:name "John" :details {:age 26}}}))

(assert (=
(increment-age {:user {:name "Jane" :details {:age 30}}})
{:user {:name "Jane" :details {:age 31}}}))

  "SUCCESS")

(tst)
