(ns among-us.controllers.fraud-check
  (:require [among-us.config :as config]
            [among-us.db.datomic.fraud-check :as db.fraud-check]
            [among-us.diplomat.http-client :as http-client]
            [among-us.diplomat.producer :as diplomat.producer]
            [among-us.logic.experiments :as experiments]
            [among-us.logic.fraud-check :as logic.fraud-check]
            [nuntio.side-effects.alpha :as side-effects]
            [schema.core :as s]))

(s/def country->run-ir-validations
  {"MX" http-client/run-ir-validations})

(s/defn check-fraud-query!
  [{{{:fraud-check/keys [customer-id]} :fraud-check
     :keys [risk-rating risk-reason]} ::side-effects/data :as input
    :locale/keys [country]}
   {:keys [config http experiment] :as components}]
  (let [risk-allows-automation? (logic.fraud-check/get-risk-allows-automation risk-rating risk-reason components)
        capture-info (when risk-allows-automation?
                       (http-client/latest-finished-capture http customer-id))
        ir-validations-fn (get country->run-ir-validations country)
        ir-validations* (when (and (config/instant-release-enabled? config) ir-validations-fn)
                          (delay (ir-validations-fn http customer-id capture-info)))
        ignore-literacy-check?* (delay (experiments/is-in-ignore-literacy-check? customer-id experiment))]
    (assoc input ::side-effects/query {:risk-allows-automation? risk-allows-automation?
                                       :capture-info capture-info
                                       :delayed/ir-validations ir-validations*
                                       :delayed/ignore-literacy-check?* ignore-literacy-check?*})))

(s/defn check-fraud-route-controller
  {::side-effects/query check-fraud-query!}
  [{{:keys [capture-info risk-allows-automation?]
     :delayed/keys [ir-validations ignore-literacy-check?*]} ::side-effects/query
    {:keys [fraud-check]} ::side-effects/data
    :time/keys [as-of]}]
  (let [fraud-check-analysis (logic.fraud-check/check-fraud-analysis fraud-check risk-allows-automation? capture-info ir-validations as-of ignore-literacy-check?*)]
    (side-effects/return-and-effects
     fraud-check-analysis
     (db.fraud-check/create fraud-check-analysis))))

(s/defn check-fraud-start-controller
  {::side-effects/query check-fraud-query!}
  [{{:keys [capture-info risk-allows-automation? ir-validations]
     :delayed/keys [ignore-literacy-check?*]} ::side-effects/query
    {:keys [fraud-check]} ::side-effects/data
    :time/keys [as-of]}]
  (let [fraud-check-analysis (logic.fraud-check/check-fraud-analysis fraud-check risk-allows-automation? capture-info ir-validations as-of ignore-literacy-check?*)]
    (side-effects/effects
     (diplomat.producer/fraud-check-finished fraud-check-analysis)
     (db.fraud-check/create fraud-check-analysis))))