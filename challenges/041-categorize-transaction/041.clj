(ns categorize-transaction)

(defn categorize-transaction
  [transaction]
  )

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
