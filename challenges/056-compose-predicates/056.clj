(ns compose-predicates)

(defn eligible-user?
  [user]
  )

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
