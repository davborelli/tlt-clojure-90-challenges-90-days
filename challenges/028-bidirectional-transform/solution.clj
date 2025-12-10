;; =============================================================================
;; 028 - BIDIRECTIONAL TRANSFORMATION (DOMAIN ↔ WIRE)
;; Level: 6/18 | Type: Adapter
;; =============================================================================

(ns challenge-028.solution
  (:require [clojure.string :as str]))

(defn kebab->camel
  "Converts kebab-case to camelCase."
  [s]
  (let [parts (str/split s #"-")]
    (str (first parts)
         (str/join "" (map str/capitalize (rest parts))))))

(defn camel->kebab
  "Converts camelCase to kebab-case."
  [s]
  (-> s
      (str/replace #"([A-Z])" "-$1")
      (str/lower-case)))

(defn domain->wire
  "Converts domain format (kebab-case keywords) to wire format (camelCase strings)."
  [data]
  (reduce-kv (fn [acc k v]
               (assoc acc (kebab->camel (name k)) v))
             {}
             data))

(defn wire->domain
  "Converts wire format (camelCase strings) to domain format (kebab-case keywords)."
  [data]
  (reduce-kv (fn [acc k v]
               (assoc acc (keyword (camel->kebab k)) v))
             {}
             data))

;; CONCEPT EXPLANATIONS
;; --------------------
;; 1. Bidirectional transformations are inverses
;; 2. kebab-case: "user-id", camelCase: "userId"
;; 3. Use regex to identify capital letters for conversion

;; REFERENCE PATTERN
;; -----------------
;; Pattern inspired by: references/adapters/exemplo5.md
;; Pattern used: Bidirectional transformations between layers

;; TESTS
;; -----

(defn -test []
  (assert (= (domain->wire {:user-id 123 :full-name "John" :email-address "j@test.com"})
             {"userId" 123 "fullName" "John" "emailAddress" "j@test.com"}))
  (assert (= (wire->domain {"userId" 456 "fullName" "Jane" "emailAddress" "jane@test.com"})
             {:user-id 456 :full-name "Jane" :email-address "jane@test.com"}))
  ;; Test round-trip
  (let [original {:user-id 789 :full-name "Bob" :email-address "bob@test.com"}]
    (assert (= (wire->domain (domain->wire original)) original)))
  (println "✓ All tests passed!"))

;; Run: (-test)
