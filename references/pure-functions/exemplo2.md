(ns abaddon.logic.authlete
  (:require [abaddon.models.authlete :as models.authlete]
            [abaddon.models.ciba :as models.ciba]
            [abaddon.models.token :as models.token]
            [abaddon.wire.in.authlete :as in.authlete]
            [clj-jwt.core :as jwt.core]
            [clojure.string :as str]
            [common-core.misc :as misc]
            [schema.core :as s]))

(s/defn ^:private remove-spaces-and-empty [str-seq]
  (->> str-seq
       (map str/trim)
       (filter (complement str/blank?))))

(s/defn ^:private merchant-source-id-in-attributes :- (s/maybe s/Str)
  [client-attributes :- [in.authlete/ClientAttribute]]
  (->> client-attributes
       (filter #(= "merchant-source-id" (% :key)))
       first
       :value))

(s/defn client->merchant-source-ids :- [s/Uuid]
  [client :- in.authlete/Client]
  (when-let [merchant-source-ids (merchant-source-id-in-attributes (:attributes client))]
    (->> (str/split merchant-source-ids #",")
         remove-spaces-and-empty
         (map parse-uuid))))

(s/defn client->first-merchant-source-id :- (s/maybe s/Uuid)
  [client :- in.authlete/Client]
  (-> (client->merchant-source-ids client)
      first))

(s/defn client-assertion->client-id :- (s/maybe s/Int)
  [client-assertion :- s/Str]
  (when (not (str/blank? client-assertion))
    (-> client-assertion
        jwt.core/str->jwt
        :claims
        :sub
        str
        Long/parseLong)))

(s/defn success-with-interaction? :- s/Bool
  [action :- models.token/Action]
  (= action "INTERACTION"))

(s/defn success-with-no-interaction? :- s/Bool
  [action :- models.token/Action]
  (or (= action "NO_INTERACTION")
      (= action "OK")))

(s/defn success? :- s/Bool
  [action :- models.token/Action]
  (or (success-with-interaction? action)
      (success-with-no-interaction? action)))

(s/defn redirect-to-uri? :- s/Bool
  [action :- models.token/Action]
  (= action "LOCATION"))

(s/defn redirect-to-uri-expecting-form? :- s/Bool
  [action :- models.token/Action]
  (= action "FORM"))

(s/defn redirect? :- s/Bool
  [action :- models.token/Action]
  (or (redirect-to-uri? action)
      (redirect-to-uri-expecting-form? action)))

(s/defn bad-request? :- s/Bool
  [action :- models.token/Action]
  (= action "BAD_REQUEST"))

(s/defn server-error? :- s/Bool
  [action :- models.token/Action]
  (= action "INTERNAL_SERVER_ERROR"))

(s/defn ^:private unauthorized? :- s/Bool
  [action :- models.token/Action]
  (= action "UNAUTHORIZED"))

(s/defn forbidden? :- s/Bool
  [action :- models.token/Action]
  (= action "FORBIDDEN"))

(s/defn ^:private invalid-client? :- s/Bool
  [action :- models.token/Action]
  (= action "INVALID_CLIENT"))

(s/defn fail? :- s/Bool
  [action :- models.token/Action]
  (or (bad-request? action)
      (server-error? action)
      (unauthorized? action)
      (forbidden? action)
      (invalid-client? action)))

(s/defn reference-customer-id->ciba-authentication :- models.ciba/AuthenticationRequest
  [request-as-map :- models.ciba/AuthenticationRequest
   reference-customer-id :- (s/maybe s/Uuid)]
  (misc/assoc-some request-as-map :login-hint (when reference-customer-id (str reference-customer-id))))

(s/defn credentials-key :- models.authlete/CredentialFlow
  [is-staging? :- s/Bool
   shared-environment? :- s/Bool
   multiple-token-environment? :- s/Bool]
  (cond (and (true? is-staging?) (false? shared-environment?))
        :authlete-server-credentials

        (true? multiple-token-environment?)
        :authlete-server-multiple-tokens-credentials

        (false? multiple-token-environment?)
        :authlete-server-single-tokens-credentials

        :else
        :authlete-server-credentials))

(s/defn server-url :- s/Keyword
  [is-staging? :- s/Bool
   shared-environment? :- s/Bool
   url-key :- s/Keyword]
  (if (and (true? is-staging?) (true? shared-environment?))
    (->> url-key
         (misc/unnamespaced)
         (misc/namespaced "authlete-shared"))
    url-key))

(s/defn credentials->header :- models.authlete/Headers
  [credentials :- models.authlete/Credentials]
  (let [authorization (str "Basic " (misc/encode-b64-str-url-unsafe (str (:api-key credentials) ":" (:api-secret credentials))))]
    {"Authorization" authorization,
     "Content-Type"  "application/json"}))