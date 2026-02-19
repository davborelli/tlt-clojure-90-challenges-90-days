(ns distributed-transaction-coordinator)

(defn coordinate-2pc
  [transaction participants config]
  )

(defn- tst []
  (let [participants {:db-orders (fn [phase data] 
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:commit :success}))
                      :db-inventory (fn [phase data] 
                                      (if (= phase :prepare) 
                                        {:prepare :yes} 
                                        {:commit :success}))
                      :db-payment (fn [phase data] 
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:commit :success}))}
        result (coordinate-2pc
                {:order-id "ORD-123" :amount 100}
                participants
                {:timeout-ms 5000})]
    (assert (= (:status result) :committed))
    (assert (= (get-in result [:phase-1-results :db-orders]) :yes))
    (assert (= (get-in result [:phase-2-results :db-orders]) :success)))

  (let [participants {:db-orders (fn [phase data] 
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:abort :success}))
                      :db-inventory (fn [phase data] 
                                      (if (= phase :prepare) 
                                        {:prepare :no :reason "Insufficient stock"} 
                                        {:abort :success}))
                      :db-payment (fn [phase data] 
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:abort :success}))}
        result (coordinate-2pc
                {:order-id "ORD-456" :amount 100}
                participants
                {:timeout-ms 5000})]
    (assert (= (:status result) :aborted))
    (assert (.contains (:abort-reason result) "db-inventory")))

  (let [participants {:db-orders (fn [phase data] 
                                    (Thread/sleep 2000)
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:abort :success}))
                      :db-payment (fn [phase data] 
                                    (if (= phase :prepare) 
                                      {:prepare :yes} 
                                      {:abort :success}))}
        result (coordinate-2pc
                {:order-id "ORD-789" :amount 100}
                participants
                {:timeout-ms 1000})]
    (assert (= (:status result) :aborted))
    (assert (= (get-in result [:phase-1-results :db-orders]) :timeout))
    (assert (>= (count (:recovery-actions result)) 1)))

  "SUCCESS")

(tst)
