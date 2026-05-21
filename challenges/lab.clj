(ns lab)

(defrecord Employee [first-name
                     last-name
                     business-unit
                     employee-id])

(def msilva
  (map->Employee {:first-name "Maria"
                  :last-name "da Silva"
                  :business-unit :ctp
                  :employee-id 12345}))

(println (assoc msilva :unrelated-data "isso não tem relação"))


