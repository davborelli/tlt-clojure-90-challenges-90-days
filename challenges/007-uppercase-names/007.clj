(ns uppercase-names)

(defn uppercase-names
  [names]
  )

(defn- tst []
  (assert (=
(uppercase-names ["john" "jane" "bob"])
["JOHN" "JANE" "BOB"]))

(assert (=
(uppercase-names ["Alice" "Charlie"])
["ALICE" "CHARLIE"]))

(assert (=
(uppercase-names [])
[]))

(assert (=
(uppercase-names ["" "test" ""])
["" "TEST" ""]))

  "SUCCESS")

(tst)
