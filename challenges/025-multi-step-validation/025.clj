(ns multi-step-validation)

(defn validate-user-multi
  [user]
  )

(defn- tst []
  (assert (=
(validate-user-multi {:name "John" :email "john@test.com" :age 25})
{:status :success :user {:name "John" :email "john@test.com" :age 25}}))

(assert (=
(validate-user-multi {:name "" :email "test@test.com" :age 25})
{:status :error :message "Name is required"}))

(assert (=
(validate-user-multi {:name "Jane" :email "invalid" :age 25})
{:status :error :message "Invalid email"}))

(assert (=
(validate-user-multi {:name "Bob" :email "bob@test.com" :age 150})
{:status :error :message "Invalid age range"}))

  "SUCCESS")

(tst)
