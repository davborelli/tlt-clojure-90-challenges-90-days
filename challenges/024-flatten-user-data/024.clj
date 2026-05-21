(ns flatten-user-data)

;; (defn flatten-profile
;;   [user-data]
;;   (let [name  (get-in user-data [:user :name])
;;         age   (get-in user-data [:user :age])
;;         email (get-in user-data [:user :email])]
;;     {:name name :age age :email email}))

;; (defn flatten-profile
;;   [user-data]
;;   (get user-data :user))

(defn flatten-profile
  [{:keys [user]}]
  user)

(defn- tst []
  (assert (=
(flatten-profile {:user {:name "John" :age 25 :email "john@example.com"}})
{:name "John" :age 25 :email "john@example.com"}))

(assert (=
(flatten-profile {:user {:name "Jane" :age 30 :email "jane@test.com"}})
{:name "Jane" :age 30 :email "jane@test.com"}))

  "SUCCESS")

(tst)
