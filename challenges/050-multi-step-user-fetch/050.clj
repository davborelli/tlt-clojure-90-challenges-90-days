(ns multi-step-user-fetch)

(defn process-user-fetch
  [user-id]
  )

(defn- tst []
  (assert (=
(process-user-fetch "USER-1")
{:status :success 
 :user {:id "USER-1" 
        :name "Alice" 
        :account-status :active 
        :last-login "2024-01-15" 
        :preferences {:theme "dark"}}}))

(assert (=
(process-user-fetch "USER-999")
{:status :error :message "User not found"}))

(assert (=
(process-user-fetch "USER-2")
{:status :error :message "Account is suspended"}))

  "SUCCESS")

(tst)
