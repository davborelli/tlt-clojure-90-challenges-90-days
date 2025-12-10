;; =============================================================================
;; 074 - GRAPHQL RESPONSE BUILDER
;; Level: 15/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter implements GraphQL-style field selection, building responses
;; with only the fields requested by the client. This reduces payload size
;; and allows clients to request exactly what they need.

(ns challenge-074.solution)

(defn build-response
  "Builds response with only requested fields (GraphQL-style).

  Field selectors:
  - Keyword: simple field (:name)
  - Map: nested selection ({:posts [:title :date]})

  Parameters:
  - data: Complete data map
  - fields: Vector of field selectors

  Returns: Map with only selected fields"
  [data fields]
  (reduce
    (fn [result field]
      (cond
        ;; Simple field selector
        (keyword? field)
        (if (contains? data field)
          (assoc result field (get data field))
          result)

        ;; Nested field selector
        (map? field)
        (let [[field-key nested-fields] (first field)
              field-data (get data field-key)]
          (cond
            ;; Field is a collection - apply to each item
            (and (coll? field-data) (not (map? field-data)))
            (assoc result field-key
                   (mapv #(build-response % nested-fields) field-data))

            ;; Field is a map - apply recursively
            (map? field-data)
            (assoc result field-key (build-response field-data nested-fields))

            ;; Field doesn't exist or wrong type
            :else
            result))

        :else
        result))
    {}
    fields))

;; TESTS
;; -----

(defn -test []
  ;; Test simple field selection
  (assert (= (build-response {:id "U1" :name "John" :email "john@example.com" :age 30}
                             [:id :name])
             {:id "U1" :name "John"})
          "Should select only requested fields")

  ;; Test nested field selection
  (let [result (build-response
                 {:id "U1"
                  :name "John"
                  :posts [{:id "P1" :title "Post 1" :date "2024-01-15" :body "Long body..."}
                          {:id "P2" :title "Post 2" :date "2024-01-16" :body "Another body..."}]}
                 [:name {:posts [:title :date]}])]
    (assert (= (:name result) "John") "Should include simple field")
    (assert (= (count (:posts result)) 2) "Should include nested collection")
    (assert (= (get-in result [:posts 0 :title]) "Post 1") "Should select nested fields")
    (assert (nil? (get-in result [:posts 0 :body])) "Should omit unselected nested fields"))

  ;; Test nested object (not collection)
  (assert (= (build-response {:id "U1" :profile {:name "John" :age 30 :email "john@example.com"}}
                             [:id {:profile [:name :age]}])
             {:id "U1" :profile {:name "John" :age 30}})
          "Should handle nested objects")

  (println "✓ All tests passed! The build-response function works correctly."))

;; Run: (-test)
