(ns complex-orchestration)

(defn orchestrate
  [workflow-config input-data]
  )

(defn- tst []
  (let [config {:steps [{:service :inventory :action :reserve}
                        {:service :payment :action :charge}
                        {:service :shipping :action :schedule}]
                :circuit-breakers {:inventory {:failure-threshold 5}
                                   :payment {:failure-threshold 3}
                                   :shipping {:failure-threshold 5}}
                :service-handlers {:inventory (fn [action data] {:reserved true})
                                   :payment (fn [action data] {:charged true :amount (:total data)})
                                   :shipping (fn [action data] {:scheduled true})}}
        result (orchestrate config {:order-id "ORD-123" :items [{:id "I1"}] :total 100})]
    (assert (= (:status result) :success))
    (assert (= (count (:results result)) 3))
    (assert (>= (count (:trace result)) 3)))

  (let [config {:steps [{:service :inventory :action :reserve}
                        {:service :payment :action :charge}
                        {:service :shipping :action :schedule}]
                :compensation {:inventory {:action :unreserve}
                               :payment {:action :refund}}
                :service-handlers {:inventory (fn [action data] 
                                                 (if (= action :reserve) 
                                                   {:reserved true}
                                                   {:released true}))
                                   :payment (fn [action data] 
                                              (if (= (:order-id data) "ORD-fail")
                                                (throw (ex-info "Card declined" {}))
                                                {:charged true}))
                                   :shipping (fn [action data] {:scheduled true})}}
        result (orchestrate config {:order-id "ORD-fail" :items [{:id "I1"}] :total 100})]
    (assert (= (:status result) :failure))
    (assert (>= (count (:compensations-executed result)) 1))
    (assert (= (:final-state result) :rolled-back)))

  "SUCCESS")

(tst)
