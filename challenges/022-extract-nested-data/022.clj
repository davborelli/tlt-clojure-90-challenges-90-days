(ns extract-nested-data)

;; (defn extract-location
;;   [user-data]
;;   {:city (get-in user-data [:user :contact :address :city])
;;    :zip  (get-in user-data [:user :contact :address :zip])})

(defn extract-location
  [user-data]
  {:city (get-in user-data [:user :contact :address :city])
   :zip (get-in user-data [:user :contact :address :zip])})

(defn- tst []
  (assert (=
(extract-location {:user {:name "John" :contact {:address {:city "NYC" :zip "10001"}}}})
{:city "NYC" :zip "10001"}))

(assert (=
(extract-location {:user {:name "Jane" :contact {:address {:city "LA" :zip "90001"}}}})
{:city "LA" :zip "90001"}))

  "SUCCESS")

(tst)
