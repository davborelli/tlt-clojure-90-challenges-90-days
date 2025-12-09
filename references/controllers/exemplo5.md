(ns cuerda.controllers.transaction
  (:require [common-core.exceptions.core :as ex]
            [common-core.protocols.config :refer [IConfig]]
            [common-core.protocols.producer :refer [IProducer]]
            [common-core.types.time :as types.time]
            [common-core.visibility :as vis]
            [common-dynamodb.protocol :as protocol-dynamodb]
            [common-i18n.time :as i18n.time]
            [common-pan-mapping.protocols.pan-mapping-provider
             :as protocols.pan-mapping-provider
             :refer [IPANMappingProvider]]
            [common-shard-mapping.protocols.shard-mapping-provider
             :refer [IShardMappingProvider]]
            [common-spei.controllers.prototype :as spei-controllers.prototype]
            [common-xp.protocols :refer [IExperiment]]
            [cuerda.controllers.experiment :as controllers.experiment]
            [cuerda.db.dynamodb.transaction :as db.transaction]
            [cuerda.diplomat.producer :as producer]
            [cuerda.logic.transaction :as logic.transaction]
            [cuerda.models.transaction :as models.transaction]
            [cuerda.wire.in.meta :as wire.in.meta]
            [cuerda.wire.in.money-in :as in.money-in]
            [cuerda.wire.in.money-out :as in.money-out]
            [schema.core :as s]))

(s/defn ^:private create-record! :- models.transaction/TransactionRecord
  [{:keys [tracking-key amount issuer beneficiary] :as money-out} :- in.money-out/MoneyOut
   as-of :- types.time/LocalDateTime
   shard-mapping-provider :- IShardMappingProvider
   pan-mapping-provider :- IPANMappingProvider
   config :- IConfig
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2]
  (when-not (logic.transaction/valid? money-out)
    (ex/invalid-input! {:reason :invalid-transaction}))

  (db.transaction/insert!
   {:tracking-key  tracking-key
    :post-date     (i18n.time/business-date as-of)
    :amount        amount
    :prototype-out (spei-controllers.prototype/account-key->prototype!
                    (:account-key issuer)
                    (:account-type issuer)
                    shard-mapping-provider
                    pan-mapping-provider)
    :prototype-in  (spei-controllers.prototype/account-key->prototype
                    (:account-key beneficiary)
                    (:account-type beneficiary)
                    shard-mapping-provider
                    pan-mapping-provider
                    config)}
   docstore-transactions))

(s/defn ^:private create-record-v2! :- models.transaction/TransactionRecord
  [{:keys [tracking-key amount issuer beneficiary] :as money-out} :- in.money-out/MoneyOut
   as-of :- types.time/LocalDateTime
   shard-mapping-provider :- IShardMappingProvider
   pan-mapping-provider :- IPANMappingProvider
   config :- IConfig
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2]
  (when-not (logic.transaction/valid? money-out)
    (ex/invalid-input! {:reason :invalid-transaction}))

  (db.transaction/insert!
   {:tracking-key  tracking-key
    :post-date     (i18n.time/business-date as-of)
    :amount        amount
    :prototype-out (spei-controllers.prototype/account-key+flow->prototype!
                    (:account-key issuer)
                    (:account-type issuer)
                    :outflow
                    shard-mapping-provider
                    pan-mapping-provider)
    :prototype-in  (spei-controllers.prototype/account-key+flow->prototype
                    (:account-key beneficiary)
                    (:account-type beneficiary)
                    :inflow
                    shard-mapping-provider
                    pan-mapping-provider
                    config)}
   docstore-transactions))

(s/defn ^:private fetch-or-create-record! :- models.transaction/TransactionRecord
  [{:keys [tracking-key] :as money-out} :- in.money-out/MoneyOut
   as-of :- types.time/LocalDateTime
   shard-mapping-provider :- IShardMappingProvider
   pan-mapping-provider :- IPANMappingProvider
   config :- IConfig
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2
   new-lending-global-routing? :- s/Bool]
  (or (db.transaction/fetch tracking-key docstore-transactions)
      (if new-lending-global-routing?
        (create-record-v2! money-out as-of shard-mapping-provider pan-mapping-provider config docstore-transactions)
        (create-record! money-out as-of shard-mapping-provider pan-mapping-provider config docstore-transactions))))

(s/defn on-send-money-out!
  [{:keys [beneficiary] :as money-out} :- in.money-out/MoneyOut
   as-of :- types.time/LocalDateTime
   metadata :- wire.in.meta/Meta
   shard-mapping-provider :- IShardMappingProvider
   pan-mapping-provider :- IPANMappingProvider
   config :- IConfig
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2
   producer :- IProducer
   xp-component :- IExperiment]
  (let [new-lending-global-routing? (controllers.experiment/new-lending-global-routing? xp-component)
        transaction (fetch-or-create-record! money-out
                                             as-of
                                             shard-mapping-provider
                                             pan-mapping-provider
                                             config
                                             docstore-transactions
                                             new-lending-global-routing?)
        {:keys [card-id customer-id]} (protocols.pan-mapping-provider/get-pan-mapping! pan-mapping-provider (:account-key beneficiary))]
    (-> money-out
        (logic.transaction/money-out->money-out-settled transaction)
        (producer/money-out-settled! transaction metadata producer))
    (-> money-out
        (logic.transaction/money-out->money-in transaction as-of card-id customer-id)
        (producer/new-money-in! transaction metadata producer))))

(s/defn on-confirm-money-in!
  [{:keys [tracking-key]} :- in.money-in/ConfirmMoneyIn
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2]
  (let [transaction (db.transaction/fetch! tracking-key docstore-transactions)]
    (vis/info :log ::on-confirm-money-in
              :transaction transaction)))

(s/defn on-return-money-in!
  [{:keys [tracking-key] :as returned-money-in} :- in.money-in/ReturnMoneyIn
   metadata :- wire.in.meta/Meta
   docstore-transactions :- protocol-dynamodb/IDynamoDBV2
   producer :- IProducer]
  (let [transaction (db.transaction/fetch! tracking-key docstore-transactions)]
    (-> returned-money-in
        (logic.transaction/returned-money-in->money-out-returned transaction)
        (producer/money-out-returned! transaction metadata producer))))