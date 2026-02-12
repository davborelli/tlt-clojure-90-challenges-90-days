(ns multi-approval-workflow)

(defn determine-approval-workflow
  [request]
  )

(defn- tst []
  (assert (=
(determine-approval-workflow
  {:amount 50000 :risk-level :critical :department :operations :requester-role :manager :has-budget-approval false :is-emergency true})
{:approvers [:board :cfo :vp] :approval-type :board :max-days 10 :requires-documentation true :escalation-required true}))

(assert (=
(determine-approval-workflow
  {:amount 75000 :risk-level :low :department :finance :requester-role :ceo :has-budget-approval true :is-emergency false})
{:approvers [] :approval-type :auto :max-days 1 :requires-documentation false :escalation-required false}))

(assert (=
(determine-approval-workflow
  {:amount 300000 :risk-level :medium :department :operations :requester-role :employee :has-budget-approval false :is-emergency false})
{:approvers [:director :vp] :approval-type :dual :max-days 5 :requires-documentation true :escalation-required false}))

  "SUCCESS")

(tst)
