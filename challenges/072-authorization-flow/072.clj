(ns authorization-flow)

(defn build-response
  [type]
  (case type
    :admin        {:authorized true  :reason "Admin access granted"}
    :authorized   {:authorized true  :reason "Owner access granted"}
    :unauthorized {:authorized false :reason "Insufficient permissions"}
    :archived     {:authorized false :reason "Cannot modify archived resource"}
    :manager      {:authorized true  :reason "Manager team access granted"}
    :team-read    {:authorized true  :reason "Team resource read access granted"}
    :public-read  {:authorized true  :reason "Public resource read access granted"}))

(defn- authorize-admin
  [_user _resource _action]
  (build-response :admin))

(defn- authorize-manager
  [_user resource action {:keys [same-team? public? archived?]}]
  (cond
    (and archived? (not= action :read)) (build-response :archived)
    (and same-team? (not= action :admin)) (build-response :manager)
    (and public? (= action :read))        (build-response :public-read)
    :else                                 (build-response :unauthorized)))

(defn- authorize-member
  [_user resource action {:keys [owner? same-team? public? team-vis? archived?]}]
  (cond
    (and archived? (not= action :read))           (build-response :archived)
    (and owner? (not= action :admin))             (build-response :authorized)
    (= action :admin)                             (build-response :unauthorized)
    (and same-team? (or team-vis? public?)
         (= action :read))                        (build-response :team-read)
    (and public? (= action :read))                (build-response :public-read)
    :else                                         (build-response :unauthorized)))

(defn- authorize-guest
  [_user _resource action {:keys [public?]}]
  (if (and public? (= action :read))
    (build-response :public-read)
    (build-response :unauthorized)))

(defn authorize
  [user resource action]
  (let [ctx {:owner?     (= (:user-id user) (:owner-id resource))
             :same-team? (= (:team-id user) (:team-id resource))
             :archived?  (= (:status resource) :archived)
             :public?    (= (:visibility resource) :public)
             :team-vis?  (= (:visibility resource) :team)}]
    (case (:role user)
      :admin   (authorize-admin   user resource action)
      :manager (authorize-manager user resource action ctx)
      :member  (authorize-member  user resource action ctx)
      :guest   (authorize-guest   user resource action ctx)
      (build-response :unauthorized))))

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
