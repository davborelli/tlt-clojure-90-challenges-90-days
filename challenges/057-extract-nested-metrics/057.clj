(ns extract-nested-metrics)

(defn extract-metrics
  [analytics-report]
  )

(defn- tst []
  (assert (=
(extract-metrics
  {:user-id "USER-123"
   :metrics {:engagement {:daily {:views 1000 :clicks 50 :shares 10}
                          :weekly {:views 7000 :clicks 350 :shares 70}}
             :revenue {:daily {:amount 250.00 :transactions 25}
                       :weekly {:amount 1750.00 :transactions 175}}}})
{:user-id "USER-123"
     :daily-views 1000
     :daily-clicks 50
     :daily-shares 10
     :weekly-views 7000
     :weekly-clicks 350
     :weekly-shares 70
     :daily-revenue 250.0
     :daily-transactions 25
     :weekly-revenue 1750.0
     :weekly-transactions 175
     :total-daily-engagement 1060
     :total-weekly-engagement 7420
     :daily-click-rate 5.0
     :weekly-click-rate 5.0}))

  "SUCCESS")

(tst)
