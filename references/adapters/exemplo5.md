(ns cuerda.adapters.transaction
  (:require [common-core.time :as time]
            [cuerda.models.transaction :as models.transaction]
            [schema.core :as s]))

(s/defn record->entry :- models.transaction/TransactionEntry
  [{:keys [tracking-key post-date amount prototype-out prototype-in]} :- models.transaction/TransactionRecord]
  {:tracking-key  tracking-key
   :post-date     (time/local-date->string post-date)
   :amount        amount
   :prototype-out (name prototype-out)
   :prototype-in  (name prototype-in)})

(s/defn entry->record :- models.transaction/TransactionRecord
  [{:keys [tracking-key post-date amount prototype-out prototype-in]} :- models.transaction/TransactionEntry]
  {:tracking-key  tracking-key
   :post-date     (time/string->local-date post-date)
   :amount        (bigdec amount)
   :prototype-out (keyword prototype-out)
   :prototype-in  (keyword prototype-in)})