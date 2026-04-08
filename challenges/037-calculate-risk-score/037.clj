(ns calculate-risk-score-simple)

(defn calculate-risk
  [transaction]
  )

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
