(ns aang.controllers.admin-avatar
  (:require [aang.db.docstore.avatar :as docstore.avatar]
            [aang.diplomat.http-client :as http-client]
            [aang.models.avatar :as models.avatar]
            [common-core.exceptions.core :as ex]
            [common-core.protocols.http-client :as protocols.http-client]
            [common-docstore.protocols.document-store :refer [IDocumentStore]]
            [rosetta-clj.core :as rosetta]
            [schema.core :as s]))

(s/defn admin-fetch-avatar-by-customer-id!
  [avatar-docstore :- IDocumentStore
   customer-id :- s/Uuid]
  (or (docstore.avatar/maybe-get-by-customer-id-not-deleted avatar-docstore customer-id)
      (ex/not-found! {:reason      :avatar-not-found
                      :customer-id customer-id})))

(s/defn admin-fetch-avatar-by-id!
  [avatar-docstore :- IDocumentStore
   avatar-id :- s/Uuid]
  (or (docstore.avatar/maybe-get-by-avatar-id avatar-docstore avatar-id)
      (ex/not-found! {:reason    :avatar-not-found
                      :avatar-id avatar-id})))

(s/defn fetch-avatar-rosetta-url! :- (s/maybe s/Str)
  [avatar :- models.avatar/Avatar
   http :- protocols.http-client/IHttpClient
   rosetta-authorizer :- rosetta/IImageAuthorizationProvider]
  (let [authorization-key (random-uuid)
        large-rosetta-key (get-in avatar [:large-rosetta-image :rosetta-key])]
    (rosetta/authorize-image-for-customer! rosetta-authorizer large-rosetta-key authorization-key)
    (http-client/fetch-rosetta-image-url http large-rosetta-key authorization-key)))

(s/defn admin-fetch-avatar-rosetta-url!
  [avatar-docstore :- IDocumentStore
   http :- protocols.http-client/IHttpClient
   rosetta-authorizer :- rosetta/IImageAuthorizationProvider
   avatar-id :- s/Uuid]
  (if-let [avatar (docstore.avatar/maybe-get-by-avatar-id avatar-docstore avatar-id)]
    (fetch-avatar-rosetta-url! avatar http rosetta-authorizer)
    (ex/not-found! {:reason    :avatar-not-found
                    :avatar-id avatar-id})))