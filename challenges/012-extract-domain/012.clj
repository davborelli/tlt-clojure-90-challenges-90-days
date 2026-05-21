(ns extract-domain
  (:require [clojure.string :as str]))

;; (defn extract-domain
;;   [email]
;;   (let [parts (str/split email #"@")]
;;     (if (>= (count parts) 2)
;;       (last parts)
;;       "")))

;; (defn extract-domain
;;   [email]
;;   (if email
;;     (let [parts (str/split email #"@")]
;;       (if (>= (count parts) 2)
;;         (second parts)
;;         ""))
;;     ""))

;; Versão mais idiomática
(defn extract-domain
  [email]
  (or (some-> email
              (str/split #"@")
              second)
      ""))

(defn- tst []
  (assert (=
(extract-domain "john@example.com")
"example.com"))

(assert (=
(extract-domain "jane@test.org")
"test.org"))

(assert (=
(extract-domain "bob@company.co.uk")
"company.co.uk"))

(assert (=
(extract-domain "invalid-email")
""))

  "SUCCESS")

(tst)
