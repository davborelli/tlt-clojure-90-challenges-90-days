(ns multi-criteria-approval)

(defn evaluate-loan-application
  [loan-application]
  )

(defn- tst []
  (assert (=
(evaluate-loan-application
  {:credit-score 780
   :annual-income 60000
   :debt-to-income-ratio 25
   :employment-status :employed
   :loan-amount 150000})
:approved))

(assert (=
(evaluate-loan-application
  {:credit-score 550
   :annual-income 40000
   :debt-to-income-ratio 35
   :employment-status :employed
   :loan-amount 100000})
:rejected))

(assert (=
(evaluate-loan-application
  {:credit-score 680
   :annual-income 55000
   :debt-to-income-ratio 32
   :employment-status :self-employed
   :loan-amount 120000})
:manual-review))

  "SUCCESS")

(tst)
