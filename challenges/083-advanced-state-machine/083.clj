(ns advanced-state-machine)

(defn process-event
  [state event event-data]
  )

(defn- tst []
  (let [result (process-event
                {:current-state :draft 
                 :context {:doc-id "DOC-123" :version 1} 
                 :history [] 
                 :definition {:transitions {:draft {:submit :review}}}}
                :submit
                {})]
    (assert (= (:current-state result) :review))
    (assert (= (get-in result [:context :doc-id]) "DOC-123"))
    (assert (= (count (:history result)) 1))
    (assert (= (:effects result) [])))

  (let [result (process-event
                {:current-state :review 
                 :context {:doc-id "DOC-123" :approvals 1 :required-approvals 2} 
                 :history []
                 :definition {:transitions {:review {:approve :approved}} 
                              :guards {:review {:approve (fn [ctx] (>= (:approvals ctx) (:required-approvals ctx)))}}}}
                :approve
                {})]
    (assert (:error result))
    (assert (= (:current-state result) :review)))

  (let [result (process-event
                {:current-state :review 
                 :context {} 
                 :history []
                 :definition {:transitions {:review {:publish :published}} 
                              :effects {:review {:publish [:send-notification :update-index]}}}}
                :publish
                {})]
    (assert (= (:current-state result) :published))
    (assert (= (set (:effects result)) #{:send-notification :update-index})))

  "SUCCESS")

(tst)
