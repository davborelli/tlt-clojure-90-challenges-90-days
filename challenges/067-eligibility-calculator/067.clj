(ns eligibility-calculator)

(def risk-multipliers
  {:critical 3.0
   :high     2.0
   :medium   1.5
   :low      1.0})

(defn calculate-eligibility
  [applicant]
  (let [{:keys [age
                health-status
                has-preexisting
                occupation-risk
                coverage-amount
                smoker]} applicant
        reason (cond
                 (> age 75)                   "Age exceeds maximum"
                 (< age 18)                   "Below minimum age"
                 (and
                  (= health-status :poor)
                  has-preexisting)            "Health risk too high"
                 (and
                  (= occupation-risk :high)
                  (> coverage-amount 500000)) "Occupation risk too high for coverage")]
    (if reason
      {:eligible false
       :risk-level nil
       :premium-multiplier nil
       :reason reason}
      (let [risk-level (cond
                         (or
                          (= health-status :poor)
                          (and smoker (> age 60)))       :critical
                         (or
                          (= health-status :fair)
                          (= occupation-risk :high)
                          (and smoker (> age 45)))       :high
                         (or (= health-status :good)
                             (= occupation-risk :medium)
                             (> coverage-amount 300000)) :medium
                         :else                           :low)
            premium-multiplier (risk-multipliers risk-level)]
        {:eligible true
         :risk-level risk-level
         :premium-multiplier premium-multiplier
         :reason "Approved"}))))

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
