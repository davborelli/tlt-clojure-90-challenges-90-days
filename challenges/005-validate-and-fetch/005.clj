(ns validate-and-fetch)

(defn validate-and-fetch
  [user-id]
  )

(defn- tst []
  (assert (=
(validate-and-fetch 1)
{:status :success :user {:id 1 :name "User 1"}}))

(assert (=
(validate-and-fetch 0)
{:status :error :message "Invalid user ID"}))

(assert (=
(validate-and-fetch -5)
{:status :error :message "Invalid user ID"}))

(assert (=
(validate-and-fetch 42)
{:status :success :user {:id 42 :name "User 42"}}))

  "SUCCESS")

(tst)
