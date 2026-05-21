(ns loan-eligibility)

;; (defn check-eligibility
;;   [applicant]
;;   (let [age          (get-in applicant [:age])
;;         income       (get-in applicant [:income])
;;         credit-score (get-in applicant [:credit-score])
;;         employed     (get-in applicant [:employed])
;;         debt         (get-in applicant [:debt])]
;;     (or
;;      (when (or (< age 18) (> age 70)) {:eligible false :reason "Age outside eligible range"})
;;      (when (< income 30000)           {:eligible false :reason "Income below minimum"})
;;      (when (< credit-score 600)       {:eligible false :reason "Credit score too low"})
;;      (when (false? employed)          {:eligible false :reason "Employment required"})
;;      (when (> debt (* income 0.4))    {:eligible false :reason "Debt-to-income ratio too high"})
;;      {:eligible true :reason "Applicant meets all criteria"})))

(defn check-eligibility
  [{:keys [age income credit-score employed debt]}]
  (let [rules [[#(or (< age 18) (> age 70)) "Age outside eligible range"]
               [#(< income 30000)           "Income below minimum"]
               [#(< credit-score 600)       "Credit score too low"]
               [#(false? employed)          "Employment required"]
               [#(> debt (* income 0.4))    "Debt-to-income ratio too high"]]]
    (or (some (fn [[check reason]]
                (when (check)
                  {:eligible false :reason reason}))
              rules)
        {:eligible true :reason "Applicant meets all criteria"})))

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
