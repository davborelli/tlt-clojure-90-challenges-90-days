(ns fetch-validate-enrich)

(defn fetch-validate-enrich
  [user-id]
  )

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
