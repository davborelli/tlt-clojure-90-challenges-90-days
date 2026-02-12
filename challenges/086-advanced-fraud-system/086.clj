(ns advanced-fraud-system)

(defn analyze-fraud
  [transaction history patterns config]
  )

(defn- tst []
  (assert (=
(analyze-fraud
  {:amount 50 :merchant "Coffee Shop" :location "Home City" :time 1400}
  [{:amount 45 :merchant "Store" :location "Home City" :time 1000} 
   {:amount 30 :merchant "Gas" :location "Home City" :time 1200}]
  []
  {:velocity-window-hours 24 :high-risk-threshold 70})
{:risk-score 15 
 :risk-level :low 
 :recommendation :approve 
 :explanation "Normal transaction pattern" 
 :features {:amount-deviation 0.2 :velocity-normal true :location-known true}}))

  (let [result (analyze-fraud
                {:amount 5000 :merchant "Electronics" :location "Foreign Country" :time 200}
                [{:amount 50 :time 150 :location "Home City"} 
                 {:amount 60 :time 160 :location "Home City"} 
                 {:amount 55 :time 170 :location "Home City"}]
                [{:pattern :high-velocity :threshold 3}]
                {})]
    (assert (>= (:risk-score result) 70))
    (assert (= (:risk-level result) :critical))
    (assert (= (:recommendation result) :reject))
    (assert (>= (count (:triggered-rules result)) 2)))

  "SUCCESS")

(tst)
