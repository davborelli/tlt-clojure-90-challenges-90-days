(ns saga-transaction)

(defn process-order
  [order]
  )

(defn- tst []
  (let [result (process-order
                {:order-id "ORD-123" :user-id "U1" :items [{:id "I1"}] :payment {:amount 100}})]
    (assert (= (:status result) :completed))
    (assert (= (:order-id result) "ORD-123"))
    (assert (= (count (:steps result)) 4)))

(assert (=
(process-order
  {:order-id "ORD-fail-payment" :user-id "U1" :items [{:id "I1"}] :payment {:amount 0}})
{:status :failed 
 :failed-step "charge-payment" 
 :compensations-run ["release-inventory"] 
 :reason "Payment failed"}))

  "SUCCESS")

(tst)
