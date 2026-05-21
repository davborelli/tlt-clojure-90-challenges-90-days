(ns bidirectional-transform 
  (:require
    [clojure.string :as str]))

(defn keyword->camel [k]
  (let [parts (str/split (name k) #"-")]
    (->> parts
         (map-indexed (fn [i part]
                        (if (= i 0)
                          part
                          (str/capitalize part))))
         (apply str))))

(defn camel->keyword [s]
  (->> s
       (re-seq #"[A-Z][a-z]*|[a-z]+")
       (map str/lower-case)
       (str/join "-")
       keyword))

(defn domain->wire
  [domain-data]
  (reduce-kv
   (fn [acc k v]
     (assoc acc (keyword->camel k) v))
   {}
   domain-data))

(defn wire->domain
  [wire-data]
  (reduce-kv
   (fn [acc k v]
     (assoc acc (camel->keyword k) v))
   {}
   wire-data)
  )

(defn- tst []
  (assert (=
(domain->wire {:user-id 123 :full-name "John" :email-address "j@test.com"})
{"userId" 123 "fullName" "John" "emailAddress" "j@test.com"}))

(assert (=
(wire->domain {"userId" 456 "fullName" "Jane" "emailAddress" "jane@test.com"})
{:user-id 456 :full-name "Jane" :email-address "jane@test.com"}))

  "SUCCESS")

(tst)
