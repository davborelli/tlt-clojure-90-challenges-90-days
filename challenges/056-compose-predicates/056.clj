(ns compose-predicates)

(defn adult?
  [user]
  (>= (:age user) 18))

(defn verified?
  [data]
  (true? (:verified data)))

(defn active?
  [data]
  (= (:account-status data) :active))

(defn onboarding-complete?
  [data]
  (true? (:onboarding-complete data)))

(defn every-pred?
  [preds val]
  (every? #(% val) preds))

(defn eligible-user?
  [user]
  (every-pred? [adult? verified? active?] user))

(defn- tst []
  (assert (=
(eligible-user?
  {:age 25
   :verified true
   :account-status :active
   :onboarding-complete true})
true))

(assert (=
(eligible-user?
  {:age 25
   :verified false
   :account-status :active
   :onboarding-complete true})
false))

(assert (=
(eligible-user?
  {:age 16
   :verified true
   :account-status :active
   :onboarding-complete true})
false))

  "SUCCESS")

(tst)
