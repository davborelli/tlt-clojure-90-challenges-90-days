(ns validate-email-format)

(defn valid-email-format?
  [email]
  )

(defn- tst []
  (assert (= (valid-email-format? "user@example.com") true))
  (assert (= (valid-email-format? "invalid.email") false))
  (assert (= (valid-email-format? "@example.com") false))
  (assert (= (valid-email-format? "user@domain") false))
  "SUCCESS")

(tst)
