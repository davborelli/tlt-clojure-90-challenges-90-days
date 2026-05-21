(ns fetch-and-sanitize
  (:require [clojure.string :as str]))

;; (defn fetch-and-sanitize
;;   [user-id]
;;   (if (and (number? user-id) (> user-id 0))
;;     {:status :success
;;      :user {:id user-id
;;             :name (str "User " user-id)
;;             :email (str "user" user-id "@example.com")}}
;;     {:status :error :message "Invalid user ID"}))

(defn valid-user-id?
  [user-id]
  (boolean (and (number? user-id) (> user-id 0))))

(defn fetch-and-sanitize
 [user-id]
 (let [user {:id user-id
             :name (str "User " user-id)
             :email (str "user" user-id "@example.com")}]
   (if (valid-user-id? user-id)
     {:status :success :user user}
     {:status :error :message "Invalid user ID"})))

(defn- tst []
  (assert (=
(fetch-and-sanitize 1)
{:status :success :user {:id 1 :name "User 1" :email "user1@example.com"}}))

(assert (=
(fetch-and-sanitize 0)
{:status :error :message "Invalid user ID"}))

(assert (=
(fetch-and-sanitize 42)
{:status :success :user {:id 42 :name "User 42" :email "user42@example.com"}}))

(assert (=
(fetch-and-sanitize -5)
{:status :error :message "Invalid user ID"}))

  "SUCCESS")

(tst)
