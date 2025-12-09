(ns aang.logic.reviews
  (:require [aang.models.reviews :as models.reviews]
            [clojure.string :as string]
            [schema.core :as s]))

(s/defn review->avatar-id :- (s/maybe s/Uuid)
  [{:keys [target]} :- models.reviews/Review]
  (when (string/starts-with? target "avatar/")
    (-> target
        (string/replace-first #"^avatar/" "")
        parse-uuid)))

(s/defn is-review-approved? :- s/Bool
  [{:keys [result]} :- models.reviews/Review]
  (= (:status result) :approved))

(s/defn is-review-denied? :- s/Bool
  [{:keys [result]} :- models.reviews/Review]
  (= (:status result) :denied))