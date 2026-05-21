(ns extract-nested-metrics)

(defn round2 
  [v]
  (/ (Math/round (* v 100.0)) 100.0))

(defn extract-metrics
  [analytics-report]
  (let [{:keys [user-id]} analytics-report
        metrics              (:metrics analytics-report)
        {eng-daily  :daily
         eng-weekly :weekly} (:engagement metrics)
        {rev-daily  :daily
         rev-weekly :weekly} (:revenue metrics)]
    {:user-id                 user-id
     :daily-views             (:views eng-daily)
     :daily-clicks            (:clicks eng-daily)
     :daily-shares            (:shares eng-daily)
     :weekly-views            (:views eng-weekly)
     :weekly-clicks           (:clicks eng-weekly)
     :weekly-shares           (:shares eng-weekly)
     :daily-revenue           (:amount rev-daily)
     :daily-transactions      (:transactions rev-daily)
     :weekly-revenue          (:amount rev-weekly)
     :weekly-transactions     (:transactions rev-weekly)
     :total-daily-engagement  (+ (:views eng-daily) (:clicks eng-daily) (:shares eng-daily))
     :total-weekly-engagement (+ (:views eng-weekly) (:clicks eng-weekly) (:shares eng-weekly))
     :daily-click-rate        (round2 (* 100 (/ (:clicks eng-daily) (:views eng-daily))))
     :weekly-click-rate       (round2 (* 100 (/ (:clicks eng-weekly) (:views eng-weekly))))}))

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
