(ns validate-email-format)

(defn valid-email-format?
  [email]
  (let [pattern #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$"]
    (boolean (and (string? email) (re-matches pattern email)))))

(defn- tst []
  (assert (= (valid-email-format? "user@example.com") true))
  (assert (= (valid-email-format? "invalid.email") false))
  (assert (= (valid-email-format? "@example.com") false))
  (assert (= (valid-email-format? "user@domain") false))
  "SUCCESS")

(tst)
