(ns loan-eligibility)

(defn check-eligibility
  [applicant]
  )

(defn- tst []
  (assert (=
(check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 10000})
{:eligible true :reason "Applicant meets all criteria"}))

(assert (=
(check-eligibility {:age 17 :income 50000 :credit-score 700 :employed true :debt 10000})
{:eligible false :reason "Age outside eligible range"}))

(assert (=
(check-eligibility {:age 30 :income 25000 :credit-score 700 :employed true :debt 5000})
{:eligible false :reason "Income below minimum"}))

(assert (=
(check-eligibility {:age 30 :income 50000 :credit-score 700 :employed true :debt 25000})
{:eligible false :reason "Debt-to-income ratio too high"}))

  "SUCCESS")

(tst)
