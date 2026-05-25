(ns format-converter
  (:require [clojure.string :as str]))

(defn database->domain
  [r]
  {:user-id   (:id r)
   :full-name (:name r)
   :email     (:email_address r)
   :active    (= (:is_active r) 1)})

(defn domain->database
  [r]
  {:id            (:user-id r)
   :name          (:full-name r)
   :email_address (:email r)
   :is_active     (if (:active r) 1 0)})

(defn api->domain 
  [r]
  {:user-id   (:userId r)
   :full-name (:fullName r)
   :email     (:email r)
   :active    (= (:status r) "active")})

(defn domain->api 
  [r]
  {:userId   (:user-id r)
   :fullName (:full-name r)
   :email    (:email r)
   :status   (if (:active r) "active" "inactive")})

(defn domain->csv
  [r]
  (str (:user-id r) "," (:full-name r) "," (:email r) "," (:active r)))

(defn csv->domain 
  [s]
  (let [[user-id full-name email active] (str/split s #",")]
    {:user-id   user-id
     :full-name full-name
     :email     email
     :active    (= active "true")}))

(defn convert-format
  [record source target]
  (if (= source target)
    record
    (let [domain (case source
                   :domain   record
                   :database (database->domain record)
                   :api      (api->domain record)
                   :csv      (csv->domain record))]
      (case target
        :domain   domain
        :database (domain->database domain)
        :api      (domain->api domain)
        :csv      (domain->csv domain)))))

(defn- tst []
  (assert (=
(convert-format
  {:id "U123" :name "John Doe" :email_address "john@example.com" :is_active 1}
  :database
  :domain)
{:user-id "U123" :full-name "John Doe" :email "john@example.com" :active true}))

(assert (=
(convert-format
  {:user-id "U456" :full-name "Jane Smith" :email "jane@example.com" :active false}
  :domain
  :api)
{:userId "U456" :fullName "Jane Smith" :email "jane@example.com" :status "inactive"}))

(assert (=
(convert-format
  {:user-id "U789" :full-name "Bob Wilson" :email "bob@example.com" :active true}
  :domain
  :csv)
"U789,Bob Wilson,bob@example.com,true"))

  "SUCCESS")

(tst)
