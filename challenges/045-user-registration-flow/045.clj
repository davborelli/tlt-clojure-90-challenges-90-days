(ns user-registration-flow
  (:require [clojure.string :as str]))

(defn- check-email-availability
  [{:keys [email] :as data}]
  (if (contains? #{"taken@example.com" "admin@example.com"} email)
    {:status :error :message "Email already registered"}
    data))

(defn- validate-input
  [{:keys [email password name age]}]
  (or
   (when (not (str/includes? email "@"))
     {:status :error :message "Validation failed: Invalid email format"})
   (when (< (count password) 8)
     {:status :error :message "Validation failed: Password must be at least 8 characters"})
   (when (str/blank? name)
     {:status :error :message "Validation failed: Name cannot be empty"})
   (when (< age 18)
     {:status :error :message "Validation failed: Must be 18 or older"})))

(defn- hash-password
  [{:keys [password] :as data}]
  (-> data
      (assoc :password-hash (str "hashed:" password))
      (dissoc :password)))

(defn- create-user-record
  [{:keys [email] :as data}]
  (assoc data :id (str "USER-" (hash email))))

(defn- build-success-response
  [{:keys [name] :as user}]
  {:status :success
   :message (str "Welcome, " name "!")
   :user user})

(defn register-user
  [registration-data]
  (or
   (validate-input registration-data)
   (let [result (check-email-availability registration-data)]
     (if (= (:status result) :error)
       result
       (-> result
           (hash-password)
           (create-user-record)
           (build-success-response))))))

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
