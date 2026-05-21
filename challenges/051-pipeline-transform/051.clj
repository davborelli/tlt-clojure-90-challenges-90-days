(ns pipeline-transform 
  (:require
    [clojure.string :as str]))

(defn trim-strings
  [user]
  (reduce-kv (fn [acc k v]
               (assoc acc k (if (string? v) (str/trim v) v)))
             {}
             user))

(defn normalize-email
  [user]
  (update user :email str/lower-case))

(defn parse-age
  [user]
  (update user :age Integer/parseInt))

(defn add-timestamp
  [user]
  (assoc user :registered-at "2024-01-15T10:00:00"))

(defn process-registration
  [user-data]
  (-> user-data
      (trim-strings)
      (normalize-email)
      (parse-age)
      (add-timestamp)))

(defn- tst []
  (assert (=
(process-registration
  {:name "  Alice Johnson  "
   :email "  ALICE@EXAMPLE.COM  "
   :age "25"})
{:name "Alice Johnson"
 :email "alice@example.com"
 :age 25
 :registered-at "2024-01-15T10:00:00"}))

(assert (=
(process-registration
  {:name "Bob Smith"
   :email "BOB@EXAMPLE.COM"
   :age "30"})
{:name "Bob Smith"
 :email "bob@example.com"
 :age 30
 :registered-at "2024-01-15T10:00:00"}))

  "SUCCESS")

(tst)
