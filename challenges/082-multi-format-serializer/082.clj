(ns multi-format-serializer)

(defn multi-serialize
  [data format operation options]
  )

(defn- tst []
  (let [result (multi-serialize
                {:user-id 123 :name "John" :roles #{:admin :user}}
                :json
                :serialize
                {:preserve-keywords true})]
    (assert (string? result))
    (assert (.contains result "user-id"))
    (assert (.contains result "123")))

  (let [original {:id 1 :tags #{:clojure :functional}}
        serialized (multi-serialize original :edn :serialize {})
        deserialized (multi-serialize serialized :edn :deserialize {})]
    (assert (= deserialized original)))

  (let [result (multi-serialize
                {:count 42 :tags #{:a :b :c}}
                :json
                :serialize
                {:preserve-keywords false})]
    (assert (map? result))
    (assert (string? (:result result)))
    (assert (>= (count (:warnings result)) 1)))

  "SUCCESS")

(tst)
