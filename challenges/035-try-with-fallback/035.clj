(ns try-with-fallback)

(defn try-fetch-with-fallback
  [user-id]
  (let [user          {:id user-id :name (str "User " user-id)}
        error-message {:status :error :message "User not found in any source"}]
    (or
     (when (and (even? user-id) (<= user-id 100)) {:status :success :source :primary   :user user})
     (when (and (even? user-id) (> user-id 100))  {:status :success :source :secondary :user user})
     error-message)))

(defn- tst []
  (assert (=
(try-fetch-with-fallback 50)
{:status :success :source :primary :user {:id 50 :name "User 50"}}))

(assert (=
(try-fetch-with-fallback 150)
{:status :success :source :secondary :user {:id 150 :name "User 150"}}))

(assert (=
(try-fetch-with-fallback 99)
{:status :error :message "User not found in any source"}))

  "SUCCESS")

(tst)
