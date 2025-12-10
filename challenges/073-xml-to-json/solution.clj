;; =============================================================================
;; 073 - XML TO JSON CONVERTER
;; Level: 15/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter converts XML-like nested structures to JSON-friendly format.
;; XML has tags, attributes, and mixed content (elements + text), while JSON
;; prefers uniform structures. We recursively process nested elements and
;; normalize the representation.

(ns challenge-073.solution
  (:require [clojure.string :as str]))

(defn kebab->camel
  "Converts kebab-case to camelCase.

  Parameters:
  - s: kebab-case string or keyword

  Returns: camelCase string"
  [s]
  (let [parts (str/split (name s) #"-")]
    (str (first parts)
         (str/join "" (map str/capitalize (rest parts))))))

(defn xml->json
  "Converts XML structure to JSON-friendly format.

  Transformations:
  - :tag → camelCase key
  - :attrs → :attributes
  - :content → recursively process children + text

  Parameters:
  - xml-data: XML map with :tag, :attrs, :content

  Returns: JSON-friendly map"
  [xml-data]
  (let [{:keys [tag attrs content]} xml-data
        tag-name (keyword (kebab->camel tag))
        children (filter map? content)
        text (str/join "" (filter string? content))
        result {}
        result (if (not-empty attrs) (assoc result :attributes attrs) result)
        result (if (not-empty text) (assoc result :text text) result)
        result (if (not-empty children)
                 (assoc result :children (mapv xml->json children))
                 result)]
    {tag-name result}))

;; TESTS
;; -----

(defn -test []
  (let [result (xml->json {:tag :user-profile
                           :attrs {:id "123"}
                           :content [{:tag :name :attrs {} :content ["John"]}
                                     {:tag :email :attrs {} :content ["john@example.com"]}]})]
    (assert (contains? result :userProfile) "Should convert tag to camelCase")
    (assert (= (get-in result [:userProfile :attributes :id]) "123") "Should preserve attributes")
    (assert (= (count (get-in result [:userProfile :children])) 2) "Should have 2 children"))

  (let [result (xml->json {:tag :simple-element :attrs {} :content ["Hello"]})]
    (assert (= (get-in result [:simpleElement :text]) "Hello") "Should extract text"))

  (println "✓ All tests passed! The xml->json function works correctly."))

;; Run: (-test)
