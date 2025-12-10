;; =============================================================================
;; 072 - AUTHORIZATION FLOW
;; Level: 15/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements complex role-based authorization with hierarchical
;; rules, ownership checks, and resource visibility controls. Authorization
;; decisions combine multiple factors: user role, resource ownership, team
;; membership, visibility settings, and resource status.

(ns challenge-072.solution)

(defn authorize
  "Evaluates authorization for user action on resource.

  Rules priority:
  1. Admin can do anything
  2. Archived resources block non-admins
  3. Ownership grants full access (except admin actions)
  4. Role-based access (manager/member/guest)

  Parameters:
  - user: Map with :user-id, :role, :team-id
  - resource: Map with :resource-id, :owner-id, :team-id, :visibility, :status
  - action: Action keyword (:read, :write, :delete, :admin)

  Returns: Map with :authorized (boolean) and :reason (string)"
  [user resource action]
  (let [{user-id :user-id user-role :role user-team :team-id} user
        {owner-id :owner-id res-team :team-id visibility :visibility status :status} resource
        is-owner (= user-id owner-id)
        same-team (and user-team res-team (= user-team res-team))
        is-archived (= status :archived)]

    (cond
      ;; Rule 1: Admin can do anything
      (= user-role :admin)
      {:authorized true :reason "Admin access granted"}

      ;; Rule 2: Archived resources (block non-admins)
      is-archived
      {:authorized false :reason "Cannot modify archived resource"}

      ;; Rule 3: Ownership (full access except admin actions)
      (and is-owner (not= action :admin))
      {:authorized true :reason "Owner access granted"}

      ;; Rule 4: Manager role (team resources)
      (and (= user-role :manager) same-team)
      {:authorized true :reason "Manager team access"}

      (and (= user-role :manager) (= action :read) (= visibility :public))
      {:authorized true :reason "Public resource access"}

      ;; Rule 5: Member role
      (and (= user-role :member) (= action :read) (= visibility :public))
      {:authorized true :reason "Public resource access"}

      (and (= user-role :member) (= action :read) same-team (#{:team :public} visibility))
      {:authorized true :reason "Team member read access"}

      (and (= user-role :member) (#{:read :write} action) is-owner)
      {:authorized true :reason "Owner access granted"}

      (and (= user-role :member) (= action :delete) is-owner)
      {:authorized true :reason "Owner delete access"}

      ;; Rule 6: Guest role (public read only)
      (and (= user-role :guest) (= action :read) (= visibility :public))
      {:authorized true :reason "Public resource access"}

      ;; Default: Deny
      :else
      {:authorized false :reason "Insufficient permissions"})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Role-Based Access Control (RBAC)
;;    Users have roles (:admin, :manager, :member, :guest) that grant
;;    different permission levels. Roles form a hierarchy:
;;    admin > manager > member > guest
;;
;; 2. Resource Ownership
;;    Owners have full control over their resources regardless of role
;;    (except admin-level actions which require admin role).
;;
;; 3. Visibility Levels
;;    Resources have visibility: :public, :team, :private
;;    This controls who can access beyond ownership.
;;
;; 4. Team-Based Access
;;    Users in the same team can access team resources based on visibility.
;;
;; 5. Audit Trail
;;    Every decision includes a reason for auditability and debugging.

;; TESTS
;; -----

(defn -test []
  ;; Admin access
  (assert (= (:authorized (authorize {:user-id "U1" :role :admin :team-id "T1"}
                                     {:owner-id "U2" :team-id "T2" :visibility :private :status :active}
                                     :delete))
             true)
          "Admin should have full access")

  ;; Owner access
  (assert (= (:authorized (authorize {:user-id "U1" :role :member :team-id "T1"}
                                     {:owner-id "U1" :visibility :private :status :active}
                                     :write))
             true)
          "Owner should have write access")

  ;; Archived blocking
  (assert (= (:authorized (authorize {:user-id "U1" :role :member :team-id "T1"}
                                     {:owner-id "U1" :visibility :private :status :archived}
                                     :write))
             false)
          "Should block archived resources for non-admins")

  ;; Guest limited access
  (assert (= (:authorized (authorize {:user-id "U1" :role :guest :team-id nil}
                                     {:owner-id "U2" :team-id "T1" :visibility :public :status :active}
                                     :read))
             true)
          "Guest should read public resources")

  (assert (= (:authorized (authorize {:user-id "U1" :role :guest :team-id nil}
                                     {:owner-id "U2" :team-id "T1" :visibility :private :status :active}
                                     :read))
             false)
          "Guest should not read private resources")

  ;; Manager team access
  (assert (= (:authorized (authorize {:user-id "U1" :role :manager :team-id "T1"}
                                     {:owner-id "U2" :team-id "T1" :visibility :team :status :active}
                                     :delete))
             true)
          "Manager should manage team resources")

  ;; Member team read
  (assert (= (:authorized (authorize {:user-id "U1" :role :member :team-id "T1"}
                                     {:owner-id "U2" :team-id "T1" :visibility :team :status :active}
                                     :read))
             true)
          "Member should read team resources")

  (println "✓ All tests passed! The authorize function works correctly."))

;; Run: (-test)
