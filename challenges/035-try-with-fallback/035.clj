(ns try-with-fallback)

(defn try-fetch-with-fallback
  [user-id]
  )

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
