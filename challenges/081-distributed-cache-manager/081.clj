(ns distributed-cache-manager)

(defn manage-cache
  [cache-state operation]
  )

(defn- tst []
  (assert (=
(manage-cache
  {:entries {} :dependencies {} :access-order [] :current-time 1000 :max-size 100}
  {:op :put :key :user-123 :value {:name "John" :age 30} :ttl 5000})
{:entries {:user-123 {:value {:name "John" :age 30} :expires-at 6000 :created-at 1000}} 
 :dependencies {} 
 :access-order [:user-123] 
 :current-time 1000 
 :max-size 100}))

  (let [result (manage-cache
                {:entries {:user-123 {:value "data1" :expires-at 9999999} 
                           :user-456 {:value "data2" :expires-at 9999999}} 
                 :dependencies {} 
                 :access-order [:user-123 :user-456] 
                 :current-time 1000 
                 :max-size 100}
                {:op :get :key :user-123})]
    (assert (= (:result result) "data1"))
    (assert (= (:access-order result) [:user-456 :user-123])))

  (let [result (manage-cache
                {:entries {:orders {:value "order-data" :expires-at 9999999}
                           :order-summary {:value "summary" :expires-at 9999999}
                           :order-stats {:value "stats" :expires-at 9999999}}
                 :dependencies {:orders #{:order-summary :order-stats}}
                 :access-order [:orders :order-summary :order-stats]
                 :current-time 1000
                 :max-size 100}
                {:op :invalidate :key :orders})]
    (assert (= (:entries result) {}))
    (assert (= (set (:invalidated result)) #{:orders :order-summary :order-stats})))

  (let [result (manage-cache
                {:entries {:user-123 {:value "data" :expires-at 2000}}
                 :dependencies {}
                 :access-order [:user-123]
                 :current-time 3000
                 :max-size 100}
                {:op :get :key :user-123})]
    (assert (= (:result result) nil)))

  "SUCCESS")

(tst)
