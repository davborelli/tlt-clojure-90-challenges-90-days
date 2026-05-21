(ns uppercase-names
  (:require [clojure.string :as str]))

(defn uppercase-names
  [names]
  (mapv str/upper-case names))

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
