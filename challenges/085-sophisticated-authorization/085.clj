(ns sophisticated-authorization)

(defn authorize
  [policy user resource action context]
  )

(defn- tst []
  (let [policy {:rules [{:id "R1" 
                         :roles #{:admin} 
                         :actions [:read :write :delete] 
                         :effect :allow}
                        {:id "R2" 
                         :conditions [(fn [u r c] (= (:department u) (:owner-dept r)))
                                      (fn [u r c] (< (:hour c) 18))]
                         :actions [:write] 
                         :effect :allow}
                        {:id "R3" 
                         :actions [:read] 
                         :effect :allow}]}]
    
    (assert (=
             (authorize policy
                        {:user-id "U123" :roles #{:admin}}
                        {:id "DOC-456" :type :document}
                        :delete
                        {})
             {:allow true 
              :reason "Rule R1: Admin role grants full access" 
              :matched-rules ["R1"]}))

    (assert (=
             (authorize policy
                        {:user-id "U123" :department "engineering"}
                        {:id "DOC-789" :owner-dept "engineering"}
                        :write
                        {:hour 14})
             {:allow true 
              :reason "Rule R2: Department match + business hours" 
              :matched-rules ["R2"]}))

    (let [result (authorize policy
                            {:user-id "U456" :roles #{:user}}
                            {:id "DOC-999"}
                            :delete
                            {})]
      (assert (= (:allow result) false))
      (assert (= (:matched-rules result) []))))

  "SUCCESS")

(tst)
