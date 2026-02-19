(ns complex-data-pipeline)

(defn execute-pipeline
  [pipeline-config data]
  )

(defn- tst []
  (let [config {:stages [:extract :transform :validate :load]
                :extract-fn (fn [item] item)
                :transform-fn (fn [item] (update item :data #(str "processed" (subs % 3))))
                :validate-fn (fn [item] (not= (:data item) "invalid"))
                :load-fn (fn [item] item)}
        result (execute-pipeline config [{:id 1 :data "raw1"} {:id 2 :data "raw2"}])]
    (assert (= (count (:successful result)) 2))
    (assert (= (count (:failed result)) 0))
    (assert (= (get-in result [:stats :total]) 2))
    (assert (= (get-in result [:stats :successful]) 2)))

  (let [config {:stages [:extract :transform :validate :load]
                :extract-fn (fn [item] item)
                :transform-fn (fn [item] item)
                :validate-fn (fn [item] (not= (:data item) "invalid"))
                :load-fn (fn [item] item)}
        result (execute-pipeline config [{:id 1 :data "valid"} {:id 2 :data "invalid"} {:id 3 :data "valid"}])]
    (assert (= (count (:successful result)) 2))
    (assert (= (count (:failed result)) 1))
    (assert (= (get-in result [:stats :total]) 3))
    (assert (= (:stage (first (:failed result))) :validate)))

  "SUCCESS")

(tst)
