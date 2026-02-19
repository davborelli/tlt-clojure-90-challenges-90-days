(ns calculate-risk-score)

(defn calculate-risk-score
  [transaction]
  )

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
