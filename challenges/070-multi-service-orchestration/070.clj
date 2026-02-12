(ns multi-service-orchestration)

(defn create-account
  [account-request]
  )

(defn- tst []
  (let [result (create-account
                {:email "user@example.com" :name "John Doe" :password "secret123" :preferences {}})]
    (assert (= (:status result) :success))
    (assert (.startsWith (:account-id result) "ACC-"))
    (assert (= (:message result) "Account created"))
    (assert (= (count (:events result)) 1)))

(assert (=
(create-account
  {:email "" :name "Jane" :password "pass"})
{:status :error :step "validate-account-data" :message "Email is required"}))

(assert (=
(create-account
  {:email "duplicate@example.com" :name "Bob" :password "pass123" :preferences {}})
{:status :error :step "check-duplicate-email" :message "Email already exists"}))

  "SUCCESS")

(tst)
