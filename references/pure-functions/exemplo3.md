(ns among-us.logic.fraud-check
  (:require [among-us.logic.capture-info :as logic.capture-info]
            [among-us.logic.country.br.fraud-check :as logic.country.br.fraud-check]
            [among-us.logic.country.mx.fraud-check :as logic.country.mx.fraud-check]
            [among-us.models.fraud-check :as models.fraud-check]
            [clojure.string :as str]
            [common-core.misc :as misc]
            [common-core.types.time :as types.time]
            [common-i18n.protocols.locale-provider :as protocols.locale-provider]
            [schema.core :as s]))

(def ^:private resolvers-list
  {:br {:get-risk-allows-automation logic.country.br.fraud-check/risk-allows-automation?}
   :mx {:get-risk-allows-automation logic.country.mx.fraud-check/risk-allows-automation?}})

(s/defn success? :- s/Bool
  [fraud-check :- models.fraud-check/FraudCheck]
  (= (:fraud-check/result fraud-check) :fraud-check.result/success))

(s/defn automated-success :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- s/Uuid
   as-of :- types.time/LocalDateTime]
  (merge fraud-check #:fraud-check{:docs-capture-id   docs-capture-id
                                   :created-at        as-of
                                   :completed-at      as-of
                                   :resolution-method :fraud-check.resolution-method/automated
                                   :result            :fraud-check.result/success}))

(s/defn automated-illiterate :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- s/Uuid
   as-of :- types.time/LocalDateTime]
  (merge fraud-check #:fraud-check{:docs-capture-id   docs-capture-id
                                   :created-at        as-of
                                   :completed-at      as-of
                                   :resolution-method :fraud-check.resolution-method/automated
                                   :result            :fraud-check.result/failure
                                   :failure-reason    :fraud-check.failure-reason/illiterate}))

(s/defn insufficient-data :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- (s/maybe s/Uuid)
   as-of :- types.time/LocalDateTime]
  (misc/assoc-some fraud-check
    :fraud-check/docs-capture-id docs-capture-id
    :fraud-check/created-at as-of
    :fraud-check/resolution-method :fraud-check.resolution-method/manual
    :fraud-check/manual-resolution-reason :fraud-check.manual-resolution-reason/insufficient-data))

(s/defn not-allowed-risk :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- (s/maybe s/Uuid)
   as-of :- types.time/LocalDateTime]
  (misc/assoc-some fraud-check
    :fraud-check/docs-capture-id docs-capture-id
    :fraud-check/created-at as-of
    :fraud-check/resolution-method :fraud-check.resolution-method/manual
    :fraud-check/manual-resolution-reason :fraud-check.manual-resolution-reason/risk))

(s/defn capture-failed :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- (s/maybe s/Uuid)
   as-of :- types.time/LocalDateTime]
  (misc/assoc-some fraud-check
    :fraud-check/docs-capture-id docs-capture-id
    :fraud-check/created-at as-of
    :fraud-check/resolution-method :fraud-check.resolution-method/manual
    :fraud-check/manual-resolution-reason :fraud-check.manual-resolution-reason/capture-failed))

(s/defn automatic-fail :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- (s/maybe s/Uuid)
   as-of :- types.time/LocalDateTime]
  (misc/assoc-some fraud-check
    :fraud-check/docs-capture-id docs-capture-id
    :fraud-check/created-at as-of
    :fraud-check/resolution-method :fraud-check.resolution-method/manual
    :fraud-check/manual-resolution-reason :fraud-check.manual-resolution-reason/automatic-fail))

(s/defn not-allowed-capture-method :- models.fraud-check/FraudCheck
  [fraud-check :- models.fraud-check/FraudCheck
   docs-capture-id :- (s/maybe s/Uuid)
   as-of :- types.time/LocalDateTime]
  (misc/assoc-some fraud-check
    :fraud-check/docs-capture-id docs-capture-id
    :fraud-check/created-at as-of
    :fraud-check/resolution-method :fraud-check.resolution-method/manual
    :fraud-check/manual-resolution-reason :fraud-check.manual-resolution-reason/capture-method))

(s/defn check-fraud-analysis
  [fraud-check risk-allows-automation? capture-info ir-validations as-of ignore-literacy-check?*]
  (let [docs-capture-id (:docs-capture-id capture-info)
        capture-completed-status (:completed-status capture-info)
        customer-illiterate? (:customer-illiterate? (:metadata capture-info))]
    (cond
      (not risk-allows-automation?)
      (not-allowed-risk fraud-check docs-capture-id as-of)

      (or (nil? capture-info)
          (nil? docs-capture-id))
      (insufficient-data fraud-check docs-capture-id as-of)

      (not (logic.capture-info/capture-method-allowed? capture-info))
      (not-allowed-capture-method fraud-check docs-capture-id as-of)

      (and (some? ir-validations)
           (not= :succeeded (get @ir-validations :status)))
      (automatic-fail fraud-check docs-capture-id as-of)

      (not= capture-completed-status :succeeded)
      (capture-failed fraud-check docs-capture-id as-of)

      (not (logic.capture-info/sufficient-data? capture-info))
      (insufficient-data fraud-check docs-capture-id as-of)

      (and
       customer-illiterate?
       (not @ignore-literacy-check?*))
      (automated-illiterate fraud-check docs-capture-id as-of)

      :else
      (automated-success fraud-check docs-capture-id as-of))))

(s/defn ^:private resolver
  [locale-provider :- protocols.locale-provider/ILocaleProvider
   resolver-name :- s/Keyword]
  (->> locale-provider
       protocols.locale-provider/country
       str/lower-case
       keyword
       (get resolvers-list)
       resolver-name))

(s/defn get-risk-allows-automation
  [risk-rating
   risk-reason
   {:keys [locale-provider]}]
  (let [resolve (resolver locale-provider :get-risk-allows-automation)]
    (resolve risk-rating risk-reason)))