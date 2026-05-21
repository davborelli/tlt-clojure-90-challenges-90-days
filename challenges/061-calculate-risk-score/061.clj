(ns calculate-risk-score)

(defn check-is-critital
  [data]
  (when (:has-fraud-history data)
    :critical))

(defn check-is-highest
  [{:keys [amount user-verified account-age-days]}]
  (when (and (> amount 10000) (or (not user-verified) (< account-age-days 30)))
    :high))

(defn check-is-medium
  [{:keys [amount user-verified account-age-days]}]
  (or
   (when (and (> amount 5000) (not user-verified))         :medium)
   (when (> amount 10000)                                  :medium)
   (when (and (not user-verified) (< account-age-days 90)) :medium)))

(defn calculate-risk-score
  [transaction]
  (let [checks [check-is-critital check-is-highest check-is-medium]]
    (or
     (some #(% transaction) checks)
     :low)))

(defn- tst []
  (assert (=
(calculate-risk-score {:amount 500 :user-verified true :account-age-days 180 :has-fraud-history false})
:low))

(assert (=
(calculate-risk-score {:amount 15000 :user-verified false :account-age-days 20 :has-fraud-history false})
:high))

(assert (=
(calculate-risk-score {:amount 1000 :user-verified true :account-age-days 5 :has-fraud-history true})
:critical))

(assert (=
(calculate-risk-score {:amount 6000 :user-verified false :account-age-days 100 :has-fraud-history false})
:medium))

  "SUCCESS")

(tst)
