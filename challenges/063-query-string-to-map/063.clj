(ns query-string-to-map)

(defn query-string->map
  [query-string]
  )

(defn- tst []
  (assert (=
(query-string->map "name=John&age=25&city=NYC")
{:name "John" :age "25" :city "NYC"}))

(assert (=
(query-string->map "status=active&verified=true")
{:status "active" :verified "true"}))

(assert (=
(query-string->map "")
{}))

  "SUCCESS")

(tst)
