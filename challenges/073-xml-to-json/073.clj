(ns xml-to-json)

(defn xml->json
  [xml-data]
  )

(defn- tst []
  (assert (=
(xml->json
  {:tag :user-profile 
   :attrs {:id "123"} 
   :content [{:tag :name :attrs {} :content ["John"]} 
             {:tag :email :attrs {} :content ["john@example.com"]}]})
{:userProfile {:attributes {:id "123"} 
               :children [{:name {:text "John"}} 
                          {:email {:text "john@example.com"}}]}}))

  "SUCCESS")

(tst)
