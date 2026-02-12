(ns graphql-response-builder)

(defn build-response
  [data fields]
  )

(defn- tst []
  (assert (=
(build-response
  {:id "U1" :name "John" :email "john@example.com" :age 30}
  [:id :name])
{:id "U1" :name "John"}))

(assert (=
(build-response
  {:id "U1" :name "John" :posts [{:id "P1" :title "Post 1" :date "2024-01-15" :body "..."}]}
  [:name {:posts [:title :date]}])
{:name "John" :posts [{:title "Post 1" :date "2024-01-15"}]}))

  "SUCCESS")

(tst)
