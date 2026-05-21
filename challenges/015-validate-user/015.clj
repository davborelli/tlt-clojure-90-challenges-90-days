(ns validate-user
  (:require [clojure.string :as str]))

;; (defn validate-name
;;   [name]
;;   (when (str/blank? name)
;;     {:status :error :message "Name cannot be empty"}))
;; 
;; (defn validate-email
;;   [email]
;;   (when-not (second (str/split email #"@"))
;;     {:status :error :message "Invalid email format"}))
;; 
;; (defn validate-age
;;   [age]
;;   (when
;;    (< age 18)
;;     {:status :error :message "User must be adult"}))
;; 
;; (def validators
;;   {:name validate-name
;;    :email validate-email
;;    :age validate-age})
;; 
;; (defn validate
;;   [type value]
;;   (if-let [validator (get validators type)]
;;     (validator value)
;;     (throw (ex-info "Unknow validation type" {:type type}))))
;; 
;; (defn validate-user
;;   [{:keys [name email age] :as user}]
;;   (or (validate :name name)
;;       (validate :email email)
;;       (validate :age age)
;;       {:status :success :user user}))

(defn validate-user
  [{:keys [name email age] :as user}]
  (cond 
    (str/blank? name) {:status :error :message "Name cannot be empty"}
    (not (str/includes? email "@")) {:status :error :message "Invalid email format"}
    (< age 18) {:status :error :message "User must be adult"}
    :else {:status :success :user user}))

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
