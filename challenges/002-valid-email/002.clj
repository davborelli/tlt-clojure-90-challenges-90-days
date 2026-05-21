(ns valid-email
  (:require [clojure.string :as str]))

(defn valid-email?
  [email]
  (str/includes? email "@")
  )

(defn- tst []
  (assert (= (valid-email? "user@example.com") true))
  (assert (= (valid-email? "invalid-email") false))
  (assert (= (valid-email? "") false))
  "SUCCESS")

(tst)
