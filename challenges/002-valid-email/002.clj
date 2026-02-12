(ns valid-email)

(defn valid-email?
  [email]
  )

(defn- tst []
  (assert (= (valid-email? "user@example.com") true))
  (assert (= (valid-email? "invalid-email") false))
  (assert (= (valid-email? "") false))
  "SUCCESS")

(tst)
