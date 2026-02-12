(ns group-by-age-range)

(defn group-by-age
  [users]
  )

(defn- tst []
  (assert (=
(group-by-age [{:name "Alice" :age 10} {:name "Bob" :age 16} {:name "Charlie" :age 30} {:name "Diana" :age 70}])
{:child [{:name "Alice" :age 10}] 
 :teen [{:name "Bob" :age 16}] 
 :adult [{:name "Charlie" :age 30}] 
 :senior [{:name "Diana" :age 70}]}))

(assert (=
(group-by-age [{:name "John" :age 25} {:name "Jane" :age 30}])
{:child [] :teen [] :adult [{:name "John" :age 25} {:name "Jane" :age 30}] :senior []}))

(assert (=
(group-by-age [])
{:child [] :teen [] :adult [] :senior []}))

  "SUCCESS")

(tst)
