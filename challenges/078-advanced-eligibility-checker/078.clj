(ns advanced-eligibility-checker)

(defn normalize-eligibility-data
  [source-data]
  )

(defn- tst []
  (assert (=
(normalize-eligibility-data
  {:source :web 
   :data {:userId "W123" :firstName "John" :lastName "Smith" :birthDate "03/15/1985" :annualIncome "75000" :creditScore "720" :employmentStatus "employed" :requestedAmount "25000"}})
{:user-id "W123" 
 :full-name "John Smith" 
 :birth-date "1985-03-15" 
 :annual-income 75000 
 :credit-score 720 
 :employment-status :employed 
 :requested-amount 25000 
 :has-collateral false 
 :source-system :web}))

(assert (=
(normalize-eligibility-data
  {:source :mobile 
   :data {:user_id "M456" :full_name "Jane Doe" :birth_date 512611200 :annual_income 95000 :credit_score 780 :employment_status "self_employed" :requested_amount 40000 :has_collateral true}})
{:user-id "M456" 
 :full-name "Jane Doe" 
 :birth-date "1986-03-28" 
 :annual-income 95000 
 :credit-score 780 
 :employment-status :self-employed 
 :requested-amount 40000 
 :has-collateral true 
 :source-system :mobile}))

  (let [result (normalize-eligibility-data
                {:source :web 
                 :data {:userId "W789" :creditScore "invalid"}})]
    (assert (= (:user-id result) "W789"))
    (assert (>= (count (:validation-errors result)) 3)))

  "SUCCESS")

(tst)
