(ns add-full-name)

;; (defn add-full-name
;;   [user]
;;   (let [first-name (:first-name user)
;;         last-name (:last-name user)]
;;     (assoc user :full-name (str first-name " " last-name))))

;; Versão mais idiomática
;; (defn add-full-name 
;;   [{:keys [first-name last-name] :as user}]
;;   (-> user
;;       (assoc :full-name (str first-name " " last-name))))

(defn add-full-name
  [{:keys [first-name last-name] :as user}]
  (-> user
      (assoc :full-name (str first-name " " last-name))))

(defn- tst []
  (assert (=
(add-full-name {:first-name "John" :last-name "Doe"})
{:first-name "John" :last-name "Doe" :full-name "John Doe"}))

(assert (=
(add-full-name {:first-name "Jane" :last-name "Smith"})
{:first-name "Jane" :last-name "Smith" :full-name "Jane Smith"}))

(assert (=
(add-full-name {:first-name "Bob" :last-name "Johnson"})
{:first-name "Bob" :last-name "Johnson" :full-name "Bob Johnson"}))

  "SUCCESS")

(tst)
