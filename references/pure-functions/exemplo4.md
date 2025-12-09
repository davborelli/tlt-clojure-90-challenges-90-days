(ns among-us.logic.country.br.fraud-check
  (:require [schema.core :as s]))

(s/defn risk-allows-automation? :- s/Bool
  [risk-rating risk-reason]
  (and (= risk-rating :low) (= risk-reason :fast-analysis-queue)))