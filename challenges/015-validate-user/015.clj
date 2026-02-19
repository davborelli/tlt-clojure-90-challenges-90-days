(ns validate-user)

(defn validate-user
  [user]
  )

(defn- tst []
  (assert (=
(validate-user {:name "John" :email "john@example.com" :age 25})
{:status :success :user {:name "John" :email "john@example.com" :age 25}}))

(assert (=
(validate-user {:name "" :email "john@example.com" :age 25})
{:status :error :message "Name cannot be empty"}))

(assert (=
(validate-user {:name "John" :email "invalid" :age 25})
{:status :error :message "Invalid email format"}))

(assert (=
(validate-user {:name "John" :email "john@example.com" :age 17})
{:status :error :message "User must be adult"}))

  "SUCCESS")

(tst)
