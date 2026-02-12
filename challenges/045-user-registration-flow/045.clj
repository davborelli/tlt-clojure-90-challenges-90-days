(ns user-registration-flow)

(defn register-user
  [user-data]
  )

(defn- tst []
  (let [result (register-user {:email "new@example.com" :password "secret123" :name "John Doe" :age 25})]
    (assert (= (:status result) :success))
    (assert (= (:message result) "Welcome, John Doe!"))
    (assert (.startsWith (:id (:user result)) "USER-"))
    (assert (= (:email (:user result)) "new@example.com")))

(assert (=
(register-user {:email "invalid" :password "secret123" :name "John" :age 25})
{:status :error :message "Validation failed: Invalid email format"}))

(assert (=
(register-user {:email "taken@example.com" :password "secret123" :name "John" :age 25})
{:status :error :message "Email already registered"}))

  "SUCCESS")

(tst)
