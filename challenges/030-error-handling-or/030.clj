(ns error-handling-or)

(defn fetch-with-fallback
  [user-id]
  (or (when (<= user-id 0) {:status :error :message "Invalid ID"}) 
      (when (>= user-id 1000) {:status :error :message "ID out of range"})
      (when (odd? user-id) {:status :error :message "User not found"})
      {:status :success :user {:id user-id :name (str "User " user-id)}})
  )

(defn- tst []
  (assert (=
(fetch-with-fallback 100)
{:status :success :user {:id 100 :name "User 100"}}))

(assert (=
(fetch-with-fallback 0)
{:status :error :message "Invalid ID"}))

(assert (=
(fetch-with-fallback 2000)
{:status :error :message "ID out of range"}))

(assert (=
(fetch-with-fallback 51)
{:status :error :message "User not found"}))

  "SUCCESS")

(tst)
