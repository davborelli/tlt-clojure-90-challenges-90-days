(ns fetch-and-sanitize)

(defn fetch-and-sanitize
  [user-id]
  )

(defn- tst []
  (assert (=
(fetch-and-sanitize 1)
{:status :success :user {:id 1 :name "User 1" :email "user1@example.com"}}))

(assert (=
(fetch-and-sanitize 0)
{:status :error :message "Invalid user ID"}))

(assert (=
(fetch-and-sanitize 42)
{:status :success :user {:id 42 :name "User 42" :email "user42@example.com"}}))

(assert (=
(fetch-and-sanitize -5)
{:status :error :message "Invalid user ID"}))

  "SUCCESS")

(tst)
