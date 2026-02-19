(ns nested-data-extractor)

(defn extract-nested-data
  [data config]
  )

(defn- tst []
  (assert (=
(extract-nested-data
  {:user {:profile {:name "John Doe" :contact {:email "john@example.com" :phone "555-1234"}} :settings {:theme "dark"}}}
  {:paths [[:user :profile :name] [:user :profile :contact :email] [:user :settings :theme]] 
   :aliases {[:user :profile :name] :name 
             [:user :profile :contact :email] :email 
             [:user :settings :theme] :theme}})
{:name "John Doe" :email "john@example.com" :theme "dark"}))

(assert (=
(extract-nested-data
  {:user {:profile {:name "Jane Smith"}}}
  {:paths [[:user :profile :name] [:user :profile :contact :email] [:user :settings :notifications]] 
   :aliases {[:user :profile :name] :name 
             [:user :profile :contact :email] :email 
             [:user :settings :notifications] :notifications}
   :defaults {[:user :profile :contact :email] "no-email@example.com" 
              [:user :settings :notifications] true}})
{:name "Jane Smith" :email "no-email@example.com" :notifications true}))

  (let [result (extract-nested-data
                {:user {:profile {:name "Bob Jones"}}}
                {:paths [[:user :profile :name] [:user :profile :id] [:user :settings :role]] 
                 :required #{[:user :profile :id] [:user :settings :role]} 
                 :aliases {[:user :profile :name] :name 
                           [:user :profile :id] :id 
                           [:user :settings :role] :role}})]
    (assert (= (:name result) "Bob Jones"))
    (assert (>= (count (:extraction-errors result)) 2)))

(assert (=
(extract-nested-data
  {:orders [{:id 1 :total 100} {:id 2 :total 200}] :user {:name "Alice"}}
  {:paths [[:orders 0 :id] [:orders 1 :total] [:user :name]] 
   :aliases {[:orders 0 :id] :first-order-id 
             [:orders 1 :total] :second-order-total 
             [:user :name] :customer-name}})
{:first-order-id 1 :second-order-total 200 :customer-name "Alice"}))

  "SUCCESS")

(tst)
