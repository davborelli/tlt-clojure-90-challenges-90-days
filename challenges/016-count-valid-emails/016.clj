(ns count-valid-emails)

(defn count-valid-emails
  [emails]
  )

(defn- tst []
  (assert (=
(count-valid-emails ["john@example.com" "invalid" "jane@test.com"])
2))

(assert (=
(count-valid-emails ["test@test.com" "" "   " "user@domain.org"])
2))

(assert (=
(count-valid-emails ["invalid" "no-at-sign" ""])
0))

(assert (=
(count-valid-emails [])
0))

  "SUCCESS")

(tst)
