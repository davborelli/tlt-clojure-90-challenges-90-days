(ns flatten-nested-user)

(defn flatten-user-profile
  [profile-data]
  )

(defn- tst []
  (assert (=
(flatten-user-profile 
  {:user-id "USER-123" 
   :personal {:name "Alice Johnson" :birthdate "1990-05-15"} 
   :contact {:email {:primary "alice@example.com" :verified true} 
             :phone {:mobile "555-0100" :country-code "+1"} 
             :address {:street "123 Main St" :city "New York" :zip "10001" :country "USA"}}})
{:id "USER-123" 
 :name "Alice Johnson" 
 :birthdate "1990-05-15" 
 :email "alice@example.com" 
 :email-verified true 
 :mobile-phone "555-0100" 
 :phone-country-code "+1" 
 :street "123 Main St" 
 :city "New York" 
 :zip "10001" 
 :country "USA"}))

  (let [result (flatten-user-profile 
                {:user-id "USER-456" 
                 :personal {:name "Bob Smith" :birthdate "1985-10-20"} 
                 :contact {:email {:primary "bob@example.com" :verified false} 
                           :phone {:mobile "555-0200" :country-code "+44"} 
                           :address {:street "456 Oak Ave" :city "London" :zip "SW1A 1AA" :country "UK"}}})]
    (assert (= (:id result) "USER-456"))
    (assert (= (:name result) "Bob Smith")))

  "SUCCESS")

(tst)
