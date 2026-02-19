(ns event-sourcing-projection)

(defn project-events
  [events initial-state]
  )

(defn- tst []
  (assert (=
(project-events
  [{:event-type :account-created :timestamp 1000 :data {:account-id "ACC-123" :owner "John Doe" :initial-balance 0}} 
   {:event-type :deposit-made :timestamp 1100 :data {:amount 1000}} 
   {:event-type :withdrawal-made :timestamp 1200 :data {:amount 200}}]
  {})
{:account-id "ACC-123" 
 :owner "John Doe" 
 :balance 800 
 :status :active 
 :version 3 
 :last-updated 1200}))

  (let [result (project-events
                [{:event-type :account-created :timestamp 1000 :data {:account-id "ACC-456" :owner "Jane Smith" :initial-balance 500}}
                 {:event-type :account-locked :timestamp 1100 :data {:reason "Security check"}}
                 {:event-type :withdrawal-made :timestamp 1150 :data {:amount 100}}
                 {:event-type :account-unlocked :timestamp 1300 :data {}}]
                {})]
    (assert (= (:account-id result) "ACC-456"))
    (assert (= (:balance result) 500))
    (assert (= (:status result) :active))
    (assert (>= (count (:rejected-events result)) 1)))

(assert (=
(project-events
  [{:event-type :account-created :timestamp 1000 :data {:account-id "ACC-789" :owner "Bob" :initial-balance 1000}}
   {:event-type :interest-accrued :timestamp 2000 :data {:rate 0.05}}]
  {})
{:account-id "ACC-789" 
 :owner "Bob" 
 :balance 1050 
 :status :active 
 :version 2 
 :last-updated 2000}))

  "SUCCESS")

(tst)
