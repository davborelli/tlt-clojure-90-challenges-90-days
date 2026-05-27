(ns multi-service-orchestration
  (:require [clojure.string :as str]))

(defn validate-account-data
  [{:keys [email name password] :as data}]
  (if-let [error-msg (cond
                       (empty? email)    "Email is required"
                       (empty? name)     "Name is required"
                       (empty? password) "Password is required")]
    {:status :error :step "validate-account-data" :message error-msg}
    (assoc data :validated true)))

(defn check-duplicate-email
  [{:keys [email] :as data}]
  (if (= (str/trim email) "duplicate@example.com")
    {:status :error :step "check-duplicate-email" :message "Email already exists"}
    (assoc data :duplicate-check-passed true)))

(defn create-account-record
  [{:keys [email] :as data}]
  (assoc data
         :account-id (str "ACC-" (hash email))
         :created-at "2024-01-15"))

(defn initialize-preferences
  [data]
  (assoc data :preferences-initialized true))

(defn send-welcome-email
  [data]
  (assoc data :welcome-email-sent true))

(defn log-creation-event
  [{:keys [email] :as data}]
  (assoc data :events [{:type :account-created :email email}]))

(defn finalize-response
  [{:keys [account-id events]}]
  {:status     :success
   :account-id account-id
   :message    "Account created"
   :events     events})

(defn create-account
  [account-request]
  (let [validated-values (validate-account-data account-request)]
    (if (= (:status validated-values) :error)
      validated-values
      (let [checked (check-duplicate-email validated-values)]
        (if (= (:status checked) :error)
          checked
          (-> checked
              (create-account-record)
              (initialize-preferences)
              (send-welcome-email)
              (log-creation-event)
              (finalize-response)))))))

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
