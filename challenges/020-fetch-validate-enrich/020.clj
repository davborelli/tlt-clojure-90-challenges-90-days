(ns fetch-validate-enrich)

(defn fetch-validate-enrich
  [user-id]
  (if (< user-id 1)
    {:status :error :message "Invalid user ID"}
    (let [user (-> {:id       user-id
                    :name     (str "User " user-id)
                    :email    (str "user" user-id "@example.com")
                    :password "secret123"}
                   (dissoc :password)
                   (assoc :fetched-at "2024-01-15T10:00:00"))]
      {:status :success :user user})))

(defn- tst []
  (assert (=
(fetch-validate-enrich 1)
{:status :success :user {:id 1 :name "User 1" :email "user1@example.com" :fetched-at "2024-01-15T10:00:00"}}))

(assert (=
(fetch-validate-enrich 42)
{:status :success :user {:id 42 :name "User 42" :email "user42@example.com" :fetched-at "2024-01-15T10:00:00"}}))

(assert (=
(fetch-validate-enrich 0)
{:status :error :message "Invalid user ID"}))

(assert (=
(fetch-validate-enrich -5)
{:status :error :message "Invalid user ID"}))

  "SUCCESS")

(tst)
