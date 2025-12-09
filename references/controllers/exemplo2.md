(ns abaddon.controllers.authlete
  (:require [abaddon.adapters.client :as adapters.client]
            [abaddon.config :as config]
            [abaddon.diplomat.http-out.authlete :as http-out.authlete]
            [abaddon.logic.authlete :as logic.authlete]
            [abaddon.logic.client :as logic.client]
            [abaddon.models.authlete :as models.authlete]
            [abaddon.models.ciba :as models.ciba]
            [abaddon.models.client :as models.client]
            [abaddon.models.token :as models.token]
            [abaddon.wire.in.authlete :as in.authlete]
            [abaddon.wire.in.authorization :as in.authorization]
            [abaddon.wire.in.oauth2 :as in.oauth2]
            [abaddon.wire.out.authlete :as out.authlete]
            [abaddon.wire.out.ciba :as out.ciba]
            [abaddon.wire.out.client :as out.client]
            [common-core.protocols.config :as protocols.config]
            [common-core.visibility :as vis]
            [common-xp.protocols :as xp.protocols]
            [nu.monads.types :refer [Either]]
            [schema.core :as s]))

(s/defn ^:private request-info! :- models.authlete/RequestInfo
  [merchant-source-id :- s/Uuid
   url-key :- s/Keyword
   config :- protocols.config/IConfig
   experiment :- xp.protocols/IExperiment]
  (let [shared-environment?         (xp.protocols/rollout-enabled? experiment :nupay-authlete-shared-environment merchant-source-id)
        multiple-token-environment? (xp.protocols/rollout-enabled? experiment :nupay-authlete-multiple-token-environment merchant-source-id)
        is-staging?                 (contains? #{"staging" "test"} (config/environment config))
        credentials-key             (logic.authlete/credentials-key is-staging? shared-environment? multiple-token-environment?)
        credentials                 (config/credentials-by-key config credentials-key)]

    (vis/info :log :request-info
              :merchant-source-id merchant-source-id
              :shared-environment? shared-environment?
              :multiple-token-environment? multiple-token-environment?
              :credentials-key credentials-key)

    {:headers    (logic.authlete/credentials->header credentials)
     :server-url (logic.authlete/server-url is-staging? shared-environment? url-key)}))

(s/defn authorize! :- in.authlete/AuthorizationResponse
  [request :- in.oauth2/AuthorizeRequest
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/oauth-authorization config experiment)]
    (http-out.authlete/authorize! request request-info external-http)))

(s/defn oauth-issue! :- in.authlete/AuthorizationIssueResponse
  [ticket :- s/Str
   reference-customer-id :- s/Uuid
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/oauth-issue config experiment)]
    (http-out.authlete/oauth-issue! ticket reference-customer-id request-info  external-http)))

(s/defn validate! :- (Either models.token/TokenValidationError models.token/TokenValidationSuccess)
  [request :- models.token/ValidQueryParams
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/token-introspection config experiment)]
    (http-out.authlete/validate! request request-info external-http)))

(s/defn token! :- (Either models.token/TokenError models.token/TokenSuccess)
  [request :- models.token/TokenRequest
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/token config experiment)]
    (http-out.authlete/token! request request-info external-http)))

(s/defn cancel! :- in.authlete/CancelAuthorizationResponse
  [request :- in.oauth2/CancelRequest
   ticket :- s/Str
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/oauth-fail config experiment)]
    (http-out.authlete/cancel! request ticket request-info external-http)))

(s/defn revoke! :- in.authlete/CancelAuthorizationResponse
  [request :- in.authorization/AuthorizationUpdateRequest
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/token-revoke config experiment)]
    (http-out.authlete/revoke! request request-info external-http)))

(s/defn client-get! :- in.authlete/Client
  [client-id :- s/Int
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/client-get config experiment)]
    (http-out.authlete/client-get! client-id request-info external-http)))

(s/defn ciba-complete! :- models.ciba/Completed
  [request :- models.ciba/Complete
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/ciba-complete config experiment)]
    (http-out.authlete/ciba-complete! request merchant-source-id request-info external-http)))

(s/defn ciba-authenticate! :- in.authlete/AuthenticationResponse
  [request :- models.ciba/AuthenticationRequest
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/ciba-authenticate config experiment)]
    (http-out.authlete/ciba-authenticate! request request-info external-http)))

(s/defn ciba-fail! :- in.authlete/AuthenticationResponse
  [request :- out.authlete/FailRequest
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/ciba-fail config experiment)]
    (http-out.authlete/ciba-fail! request request-info external-http)))

(s/defn ciba-issue! :- out.ciba/AuthenticationResponse
  [request :- s/Any
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/ciba-issue config experiment)]
    (http-out.authlete/ciba-issue! request request-info external-http)))

(s/defn jwks! :- [in.authlete/JwkResponse]
  [merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/jwk-get config experiment)]
    (http-out.authlete/jwks! request-info external-http)))

(s/defn request-update-client-secret! :- out.client/UpdateClientSecretResponse
  [request :- models.client/UpdateClientSecret
   client-id :- s/Str
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/update-client-secret config experiment)]
    (http-out.authlete/update-client-secret! request client-id request-info external-http)))

(s/defn find-customer-authorizations! :- in.authlete/FindCustomerAuthorizationsResponse
  [subject :- s/Str
   merchant-source-id :- s/Uuid
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! merchant-source-id :authlete/get-customer-authorizations config experiment)]
    (http-out.authlete/find-customer-authorizations! subject request-info external-http)))

(s/defn create-client! :- models.client/ClientCreate
  [{:keys [merchant-source-ids integration-type] :as request} :- models.client/ClientCreateRequest
   {:keys [external-http config experiment]} :- {s/Keyword s/Any}]
  (let [request-info (request-info! (first merchant-source-ids) :authlete/client-create config experiment)
        recipe (logic.client/integration-type->recipe integration-type)
        request-body (adapters.client/internal->create-client recipe request)]
    (http-out.authlete/create-client! request-body request-info external-http)))