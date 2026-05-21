(ns calculate-risk-score-simple)

(defn calculate-risk
  [{:keys [amount user-age account-age-days]}]
  (let [amount-score      (+ (if (> amount 10000) 30 0)
                             (if (> amount 5000)  20 0)
                             (if (> amount 1000)  10 0))
        age-score         (if (< user-age 21) 15 0)
        account-age-score (cond
                            (< account-age-days 30) 25
                            (< account-age-days 90) 10
                            :else 0)
        score               (+ amount-score age-score account-age-score)
        level               (cond
                              (<= score 20) :low
                              (<= score 40) :medium
                              :else         :high)]
    {:score score :level level}))

(defn- tst []
  (assert (=
(calculate-risk {:amount 500 :user-age 25 :account-age-days 100})
{:score 0 :level :low}))

(assert (=
(calculate-risk {:amount 6000 :user-age 19 :account-age-days 20})
{:score 70 :level :high}))

(assert (=
(calculate-risk {:amount 2000 :user-age 30 :account-age-days 50})
{:score 20 :level :low}))

  "SUCCESS")

(tst)
