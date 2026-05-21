(ns multi-step-user-fetch 
  (:require
    [clojure.string :as str]))

(defn fetch-user
  [user-id]
  (let [user-database {:user-1 {:id "USER-1" :name "Alice" :account-status :active}
                       :user-2 {:id "USER-2" :name "Bob" :account-status :suspended}
                       :user-3 {:id "USER-3" :name "Charlie" :account-status :closed}}]
    (get user-database (keyword (str/lower-case user-id)))))

(defn validate-status
  [{:keys [account-status]}]
  (cond
    (= (name account-status) "suspended") {:status :error :message "Account is suspended"}
    (= (name account-status) "closed")    {:status :error :message "Account is closed"}
    :else nil))

(defn enrich-profile
  [user]
  (assoc user
         :last-login "2024-01-15"
         :preferences {:theme "dark"}))

(defn format-response
  [user]
  {:status :success
   :user user})

(defn process-user-fetch
  [user-id]
  (let [user (fetch-user user-id)]
    (if user
      (or (validate-status user)
          (-> user
              (enrich-profile)
              (format-response)))
      {:status :error :message "User not found"})))

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
