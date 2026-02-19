(ns extract-domain)

(defn extract-domain
  [email]
  )

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
