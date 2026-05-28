(ns fraud-detection-system 
  (:require
    [clojure.string :as str]))

(defn add-signal
  [transaction score text]
  (-> transaction
      (update :fraud-score + score)
      (update :signals conj text)))

(defn detect-fraud
  [{:keys [amount
           is-high-risk-country
           velocity-24h
           device-known
           amount-deviation-percent
           time-since-last-tx-minutes
           user-age-days]}]
  (let [{:keys [fraud-score signals]} (cond-> {:fraud-score 0 :signals []}
                                        (> amount 10000)                 (add-signal 20 "Large transaction amount")
                                        is-high-risk-country             (add-signal 30 "High-risk country")
                                        (> velocity-24h 10)              (add-signal 25 "Unusual transaction velocity")
                                        (not device-known)               (add-signal 15 "Unknown device")
                                        (> amount-deviation-percent 200) (add-signal 20 "Amount deviation from normal")
                                        (< time-since-last-tx-minutes 5) (add-signal 15 "Rapid successive transactions")
                                        (< user-age-days 30)             (add-signal 10 "New account"))
        risk-level (cond
                     (<= fraud-score 25) :low
                     (<= fraud-score 50) :medium
                     (<= fraud-score 75) :high
                     :else                            :critical)
        action (case risk-level
                 :low      :approve
                 :medium   :review
                 :high     :review
                 :critical :block)]
    {:fraud-score fraud-score
     :risk-level risk-level
     :action action
     :signals signals
     :reason (if (empty? signals)
               "No fraud signals detected"
               (str/join ", " signals))}))

(println (detect-fraud
          {:amount 5000 :location-country "US" :is-high-risk-country false :velocity-24h 2 :device-known true :amount-deviation-percent 50 :time-since-last-tx-minutes 120 :user-age-days 365}))


(println (detect-fraud
 {:amount 15000 :location-country "XX" :is-high-risk-country true :velocity-24h 12 :device-known false :amount-deviation-percent 300 :time-since-last-tx-minutes 2 :user-age-days 15}))

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
