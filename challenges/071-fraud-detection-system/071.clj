(ns fraud-detection-system)

(defn detect-fraud
  [transaction]
  )

(defn- tst []
  (assert (=
(detect-fraud
  {:amount 5000 :location-country "US" :is-high-risk-country false :velocity-24h 2 :device-known true :amount-deviation-percent 50 :time-since-last-tx-minutes 120 :user-age-days 365})
{:fraud-score 0 :risk-level :low :action :approve :signals [] :reason "No fraud signals detected"}))

  (let [result (detect-fraud
                {:amount 15000 :location-country "XX" :is-high-risk-country true :velocity-24h 12 :device-known false :amount-deviation-percent 300 :time-since-last-tx-minutes 2 :user-age-days 15})]
    (assert (>= (:fraud-score result) 100))
    (assert (= (:risk-level result) :critical))
    (assert (= (:action result) :block)))

  "SUCCESS")

(tst)
