(ns query-string-to-map 
  (:require
    [clojure.string :as str]))

;; (defn query-string->map
;;   [query-string]
;;   (if (empty? query-string) {}
;;       (let [splited-value (str/split query-string #"&")]
;;         (reduce (fn [acc pair]
;;                      (let [[k v] (str/split pair #"=")]
;;                        (assoc acc (keyword k) v)))
;;                    {}
;;                    splited-value))))

;; (defn query-string->map [query-string]
;;   (if (empty? query-string)
;;     {}
;;     (into {}
;;           (map (fn [pair]
;;                  (println pair)
;;                  (let [[k v] (str/split pair #"=")]
;;                    [(keyword k) v]))
;;                (str/split query-string #"&")))))

;; (defn query-string->map
;;   [query-string]
;;   (if (empty? query-string)
;;     {}
;;     (into {} (map (fn [item]
;;                     (let [[k v] (str/split item #"=")]
;;                       [(keyword k) v]))
;;                   (str/split query-string #"&")))))


(defn query-string->map
  [query-string]
  (if (empty? query-string)
    {}
    (into {}
          (for [item (str/split query-string #"&")
                :let [[k v] (str/split item #"=")]]
            [(keyword k) v]))))

(defn- tst []
  (assert (=
(query-string->map "name=John&age=25&city=NYC")
{:name "John" :age "25" :city "NYC"}))

(assert (=
(query-string->map "status=active&verified=true")
{:status "active" :verified "true"}))

(assert (=
(query-string->map "")
{}))

  "SUCCESS")

(tst)
