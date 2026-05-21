(ns restructure-api-response)

(defn restructure-response
  [api-response]
  (let [users (get-in api-response [:data :users])]
    (->> users
         (map (fn [user]
                {:user-id (:id user)
                 :name    (get-in user [:profile :name])
                 :email   (get-in user [:profile :contact :email])
                 :phone   (get-in user [:profile :contact :phone])})))))

(defn- tst []
  (assert (=
(restructure-response 
  {:status "success" 
   :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 2} 
   :data {:users [{:id 1 :profile {:name "John Doe" :contact {:email "john@example.com" :phone "555-0100"}}} 
                  {:id 2 :profile {:name "Jane Smith" :contact {:email "jane@example.com" :phone "555-0200"}}}]}})
[{:user-id 1 :name "John Doe" :email "john@example.com" :phone "555-0100"} 
 {:user-id 2 :name "Jane Smith" :email "jane@example.com" :phone "555-0200"}]))

  "SUCCESS")

(tst)
