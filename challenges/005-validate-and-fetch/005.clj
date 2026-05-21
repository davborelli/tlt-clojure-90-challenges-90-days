(ns validate-and-fetch)

(defn valid-user?
  [user-id]
  (and (number? user-id) (> user-id 0)))

(defn fetch-user
  [user-id]
  {:id user-id
   :name (str "User " user-id)})

(defn validate-and-fetch
  [user-id]
    (if (valid-user? user-id)
      {:status :success
       :user (fetch-user user-id)}
      {:status :error
       :message "Invalid user ID"}))

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
