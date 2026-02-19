(ns multi-protocol-adapter)

(defn adapt-protocol
  [request handler]
  )

(defn- tst []
  (let [get-user-fn (fn [params] {:user-id (:user-id params) :name "John"})
        result (adapt-protocol
                {:protocol :http 
                 :operation :get-user 
                 :params {:user-id "123"} 
                 :headers {"Accept" "application/json"}}
                get-user-fn)]
    (assert (= (:status result) :success))
    (assert (= (get-in result [:data :user-id]) "123"))
    (assert (= (get-in result [:protocol-specific :http-status]) 200)))

  (let [get-user-fn (fn [params] {:user-id (:user-id params) :name "Jane"})
        result (adapt-protocol
                {:protocol :grpc 
                 :operation :get-user 
                 :params {:user-id "456"}}
                get-user-fn)]
    (assert (= (:status result) :success))
    (assert (= (get-in result [:data :name]) "Jane"))
    (assert (= (get-in result [:protocol-specific :grpc-code]) "OK")))

  (let [execute-graphql-fn (fn [params] {:user {:name "Bob" :email "bob@example.com"}})
        result (adapt-protocol
                {:protocol :graphql 
                 :operation :query 
                 :params {:query "{ user(id: \"789\") { name email } }"}}
                execute-graphql-fn)]
    (assert (= (:status result) :success))
    (assert (= (get-in result [:data :user :name]) "Bob")))

  "SUCCESS")

(tst)
