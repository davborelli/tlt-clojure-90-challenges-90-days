(ns multi-criteria-approval)

(defn evaluate-loan-application
  [loan-application]
  (let [{:keys [credit-score
                annual-income
                debt-to-income-ratio
                employment-status
                loan-amount]} loan-application]
    (cond
      (and (>= credit-score 800)
           (>= annual-income 75000)
           (< debt-to-income-ratio 40))     :approved
      (and (>= credit-score 750)
           (>= annual-income 50000)
           (< debt-to-income-ratio 30)
           (= employment-status :employed)) :approved
      (< credit-score 600)                  :rejected
      (>= debt-to-income-ratio 50)          :rejected
      (> loan-amount (* annual-income 5))   :rejected
      (= employment-status :unemployed)     :rejected
      :else                                 :manual-review)))
      

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
