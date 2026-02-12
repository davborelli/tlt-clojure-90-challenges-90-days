(ns keywords-to-strings)

(defn keywords->strings
  [data]
  )

(defn- tst []
  (assert (=
(keywords->strings {:name "John" :age 25})
{"name" "John" "age" 25}))

(assert (=
(keywords->strings {:user-id 123 :email "test@example.com"})
{"user-id" 123 "email" "test@example.com"}))

(assert (=
(keywords->strings {})
{}))

  "SUCCESS")

(tst)
