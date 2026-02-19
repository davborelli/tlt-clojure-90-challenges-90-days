(ns eligibility-calculator)

(defn calculate-eligibility
  [applicant]
  )

(defn- tst []
  (assert (=
(calculate-eligibility
  {:age 30 :health-status :excellent :occupation-risk :low :coverage-amount 200000 :has-preexisting false :smoker false})
{:eligible true :risk-level :low :premium-multiplier 1.0 :reason "Approved"}))

(assert (=
(calculate-eligibility
  {:age 80 :health-status :good :occupation-risk :low :coverage-amount 100000 :has-preexisting false :smoker false})
{:eligible false :risk-level nil :premium-multiplier nil :reason "Age exceeds maximum"}))

(assert (=
(calculate-eligibility
  {:age 50 :health-status :fair :occupation-risk :medium :coverage-amount 350000 :has-preexisting false :smoker true})
{:eligible true :risk-level :high :premium-multiplier 2.0 :reason "Approved"}))

  "SUCCESS")

(tst)
