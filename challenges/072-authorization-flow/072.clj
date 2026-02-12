(ns authorization-flow)

(defn authorize
  [user resource action]
  )

(defn- tst []
  (assert (=
(authorize
  {:user-id "U1" :role :admin :team-id "T1"}
  {:resource-id "R1" :owner-id "U2" :team-id "T2" :visibility :private :status :active}
  :delete)
{:authorized true :reason "Admin access granted"}))

(assert (=
(authorize
  {:user-id "U1" :role :member :team-id "T1"}
  {:resource-id "R1" :owner-id "U1" :visibility :private :status :active}
  :write)
{:authorized true :reason "Owner access granted"}))

(assert (=
(authorize
  {:user-id "U1" :role :guest :team-id nil}
  {:resource-id "R1" :owner-id "U2" :team-id "T1" :visibility :private :status :active}
  :read)
{:authorized false :reason "Insufficient permissions"}))

  "SUCCESS")

(tst)
