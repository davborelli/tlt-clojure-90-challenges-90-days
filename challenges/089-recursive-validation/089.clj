(ns recursive-validation)

(defn validate-schema
  [schema data]
  )

(defn- tst []
  (let [email-validator (fn [value] (and (string? value) (.contains value "@")))]
    (assert (=
             (validate-schema
              {:type :map 
               :required #{:name :email} 
               :fields {:name {:type :string} 
                        :email {:type :string :validators [email-validator]}}}
              {:name "John" :email "john@example.com"})
             {:valid true :errors [] :error-count 0})))

  (assert (=
(validate-schema
  {:type :map 
   :fields {:user {:type :map 
                   :required #{:name :age} 
                   :fields {:name {:type :string} 
                            :age {:type :number}}}}}
  {:user {:name "John"}})
{:valid false 
 :errors [{:path [:user :age] :message "Required field missing"}] 
 :error-count 1}))

  (let [tree-schema {:type :map
                     :required #{:value}
                     :fields {:value {:type :number}
                              :children {:type :list
                                         :item-schema :self}}}
        result (validate-schema 
                tree-schema
                {:value 10 
                 :children [{:value 5 :children []} 
                            {:value 15 :children [{:value 12 :children []}]}]})]
    (assert (= (:valid result) true))
    (assert (= (:error-count result) 0)))

  "SUCCESS")

(tst)
