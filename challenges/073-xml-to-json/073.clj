(ns xml-to-json
  (:require [clojure.string :as str]))

(declare xml->json)

(defn- kebab->camel 
  [kebab-keyword]
  (let [parts (str/split (name kebab-keyword) #"-")
        first-part (first parts)
        rest-parts (map clojure.string/capitalize (rest parts))]
    (keyword (apply str first-part rest-parts))))

(defn- extract-text
  [content]
  (let [texts (filter string? content)]
    (when (seq texts)
      {:text (str/join " " texts)})))

(defn- extract-children
  [content]
  (let [child-elements (filter map? content)]
    (when (seq child-elements)
      {:children (mapv xml->json child-elements)})))

(defn xml->json 
  [xml-data]
  (let [tag-name      (kebab->camel (:tag xml-data))
        attrs         (:attrs xml-data)
        content       (:content xml-data)
        text-part     (extract-text content)
        children-part (extract-children content)
        body (merge
              (when (seq attrs) {:attributes attrs})
              text-part
              children-part)]
    {tag-name body}))

(println (xml->json
          {:tag :user-profile
           :attrs {:id "123"}
           :content [{:tag :name :attrs {} :content ["John"]}
                     {:tag :email :attrs {} :content ["john@example.com"]}]}))

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
