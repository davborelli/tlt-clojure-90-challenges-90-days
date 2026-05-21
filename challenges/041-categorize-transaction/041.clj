(ns categorize-transaction)

(defn categorize-transaction
  [transaction]
  (let [type              (get-in transaction [:type])
        amount            (get-in transaction [:amount])
        merchant-category (get-in transaction [:merchant-category])
        priority          (get-in transaction [:priority])]
    (or
     (when (true? priority)                         :urgent)
     (when (> amount 5000)                          :high-value)
     (when (and (= type :transfer) (> amount 1000)) :high-value)
     (when (= merchant-category "travel")           :travel)
     (when (= merchant-category "restaurant")       :dining)
     (when (= merchant-category "retail")           :shopping)
     (when (= type :bill-payment)                   :bills)
     :other)))

(categorize-transaction {:type :purchase :amount 100 :merchant-category "restaurant" :priority false})

(defn- tst []
  (assert (=
(categorize-transaction {:type :purchase :amount 100 :merchant-category "restaurant" :priority false})
:dining))

(assert (=
(categorize-transaction {:type :transfer :amount 10000 :merchant-category "bank" :priority false})
:high-value))

(assert (=
(categorize-transaction {:type :purchase :amount 50 :merchant-category "other" :priority true})
:urgent))

(assert (=
(categorize-transaction {:type :bill-payment :amount 200 :merchant-category "utilities" :priority false})
:bills))

  "SUCCESS")

(tst)
