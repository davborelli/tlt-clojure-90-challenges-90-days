(ns cuerda.logic.transaction
  (:require [common-core.misc :as misc]
            [common-core.types.time :as types.time]
            [cuerda.config :as config]
            [cuerda.models.transaction :as models.transaction]
            [cuerda.wire.in.money-in :as in.money-in]
            [cuerda.wire.in.money-out :as in.money-out]
            [cuerda.wire.out.money-in :as out.money-in]
            [cuerda.wire.out.money-out :as out.money-out]
            [schema.core :as s]))

(s/defn valid? :- s/Bool
  "Cuerda can perform only internal SPEI transactions"
  [{:keys [beneficiary tracking-issuer]} :- in.money-out/MoneyOut]
  (and (contains? config/nu-institution-codes (:institution beneficiary))
       (contains? config/nu-institution-codes tracking-issuer)))

(s/defn money-out->money-in :- out.money-in/MoneyIn
  [{:keys [tracking-issuer tracking-key amount beneficiary issuer details]} :- in.money-out/MoneyOut
   {:keys [post-date]} :- models.transaction/TransactionRecord
   as-of :- types.time/LocalDateTime
   card-id :- (s/maybe s/Uuid)
   customer-id :- (s/maybe s/Uuid)]
  (-> {:tracking-issuer tracking-issuer
       :post-date       post-date
       :tracking-key    tracking-key
       :amount          amount
       :issuer          (assoc issuer :institution tracking-issuer)
       :beneficiary     (dissoc beneficiary :institution)
       :details         details
       :requested-at    as-of
       :provider        :internal/cuerda}
      (misc/assoc-some :card-id card-id
                       :customer-id customer-id)))

(s/defn money-out->money-out-settled :- out.money-out/MoneyOutSettled
  [{:keys [tracking-issuer tracking-key]} :- in.money-out/MoneyOut
   {:keys [post-date]} :- models.transaction/TransactionRecord]
  {:tracking-issuer tracking-issuer
   :tracking-key    tracking-key
   :post-date       post-date
   :status          :settled})

(s/defn returned-money-in->money-out-returned :- out.money-out/MoneyOutReturned
  [{:keys [tracking-issuer tracking-key return-reason]} :- in.money-in/ReturnMoneyIn
   {:keys [amount]} :- models.transaction/TransactionRecord]
  {:tracking-issuer tracking-issuer
   :tracking-key    tracking-key
   :amount          amount
   :return-reason   return-reason})