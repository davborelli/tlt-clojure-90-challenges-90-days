(ns snake-to-kebab
  (:require [clojure.string :as str]))

;; (defn snake->kebab
;;   [data]
;;   (update-keys data (fn [k]
;;                       (-> k
;;                           name
;;                           (str/replace "_" "-")
;;                           keyword))))

(defn snake->kebab
  [data]
  (update-keys data (fn [k]
                      (-> k
                          name
                          (str/replace "_" "-")
                          keyword))))

(defn- tst []
  (assert (=
(snake->kebab {:first_name "John" :last_name "Doe"})
{:first-name "John" :last-name "Doe"}))

(assert (=
(snake->kebab {:user_id 123 :email_address "test@example.com"})
{:user-id 123 :email-address "test@example.com"}))

(assert (=
(snake->kebab {:name "Alice"})
{:name "Alice"}))

(assert (=
(snake->kebab {})
{}))

  "SUCCESS")

(tst)
