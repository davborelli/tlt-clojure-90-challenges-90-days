(ns payment-pipeline)

(defn process-payment
  [payment-request]
  )

(defn- tst []
  (assert (=
           (:status (process-payment
                     {:user-id "USER-123"
                      :amount 100.00
                      :recipient "MERCHANT-456"}))
           :completed))
  
  (let [result (process-payment
                {:user-id "USER-123"
                 :amount 100.00
                 :recipient "MERCHANT-456"})]
    (assert (= (:user-id result) "USER-123"))
    (assert (= (:amount result) 100.0))
    (assert (= (:fee result) 2.0))
    (assert (= (:total result) 102.0))
    (assert (.startsWith (:transaction-id result) "TXN-USER-123-"))
    (assert (= (:status result) :completed)))

  (assert (=
(process-payment
  {:user-id "USER-456"
   :amount 0
   :recipient "MERCHANT-789"})
{:status :error :message "Invalid amount"}))

  "SUCCESS")

(tst)
