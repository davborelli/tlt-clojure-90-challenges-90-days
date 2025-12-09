(ns abaddon.adapters.authlete
  (:require [abaddon.models.ciba :as models.ciba]
            [abaddon.models.token :as models.token]
            [abaddon.wire.in.authlete :as in.authlete]
            [abaddon.wire.in.authorization :as in.authorization]
            [abaddon.wire.in.ciba :as in.ciba]
            [abaddon.wire.in.oauth2 :as in.oauth2]
            [abaddon.wire.out.authlete :as out.authlete]
            [abaddon.wire.out.authorization :as out.authorization]
            [abaddon.wire.out.ciba :as out.ciba]
            [abaddon.wire.out.oauth2 :as out.oauth2]
            [camel-snake-kebab.core :as kebab.core]
            [camel-snake-kebab.extras :as kebab.extras]
            [cheshire.core :as cheshire]
            [clojure.string :as str]
            [common-core.misc :as misc]
            [common-core.serializer.json :as serializer.json]
            [common-core.types.numeric :as types.numeric]
            [ring.util.codec :as util.codec]
            [schema.core :as s]))

(s/defn ^:private scopes->str
  [scopes :- s/Any]
  (if (vector? scopes)
    (str/join " " scopes)
    scopes))

(s/defn ->camelCase :- {s/Keyword s/Any}
  [payload :- {s/Keyword s/Any}]
  (update-keys payload kebab.core/->camelCaseKeyword))

(def status-codes
  {:INTERNAL_SERVER_ERROR 500
   :ERROR                 500
   :INVALID_TICKET        500
   :FORM                  501
   :NOT_IMPLEMENTED       501
   :FORBIDDEN             403
   :UNAUTHORIZED          401
   :BAD_REQUEST           400
   :INVALID_CLIENT        400
   :UNPROCESSABLE_ENTITY  422
   :LOCATION              302
   :OK                    200
   :USER_IDENTIFICATION   200})

(s/defn action->status-code :- s/Int
  [action :- (s/cond-pre s/Str s/Keyword)]
  (get status-codes (keyword action) 500))

(s/defn token-validate-action->status-code :- (s/maybe s/Int)
  [action :- s/Str]
  (get (assoc status-codes :BAD_REQUEST 401) (keyword action)))

(s/defn ^:private state-decoded :- (s/maybe s/Str)
  [state :- s/Str]
  (when (not-empty state)
    (util.codec/url-decode state)))

(s/defn in->authorization-redirect-to :- out.oauth2/RedirectTo
  [response-content :- s/Str]
  {:redirect-uri response-content})

(s/defn ^:private map->query-string :- s/Any
  [payload :- s/Any]
  (-> payload
      (update-keys #(-> % misc/dash->underscore name))
      util.codec/form-encode))

(s/defn ^:private map->json-url-encoded :- s/Str
  [payload :- s/Any]
  (->> (map misc/dash->underscore payload)
       serializer.json/write-json
       util.codec/url-encode))

(s/defn ^:private in->authlete-scope :- s/Str
  [auth-request :- in.oauth2/AuthorizeRequest]
  (str "&scope="
       (str/join "+" (:scope auth-request))))

(s/defn ^:private in->authlete-authorization-details :- s/Str
  [auth-request :- in.oauth2/AuthorizeRequest]
  (str "&authorization_details="
       (map->json-url-encoded (:authorization-details auth-request))))

(s/defn in->authorization-request :- out.authlete/AuthorizationRequest
  [auth-request :- in.oauth2/AuthorizeRequest]
  {:parameters (str (-> (assoc auth-request :response_type "code"
                               :prompt "consent"
                               :redirect-uri (str/lower-case (:redirect-uri auth-request)))
                        (dissoc :scope)
                        (dissoc :authorization-details)
                        map->query-string)
                    (in->authlete-scope auth-request)
                    (in->authlete-authorization-details auth-request))})

(s/defn wire-in->validate-query-params :- models.token/ValidQueryParams
  [{:keys [token scope]} :- in.authorization/TokenValidQueryParams]
  {:token token
   :scope scope})

(s/defn wire-in->oauth2-issue-request :- out.authlete/OAuth2IssueRequest
  [ticket :- s/Str
   customer-id :- s/Uuid]
  {:subject (str customer-id)
   :ticket  ticket})

(s/defn in->token-introspection-request :- out.authlete/TokenIntrospectionRequest
  [{:keys [token scope]} :- models.token/ValidQueryParams]
  {:token  token
   :scope scope})

(s/defn json-response->key-map :- (s/maybe {s/Keyword s/Any})
  [response :- (s/maybe {s/Keyword s/Any})]
  (-> response
      (update :body (fnil (partial kebab.extras/transform-keys kebab.core/->kebab-case-keyword) {}))
      :body))

(def response-content->map (misc/partialr cheshire/parse-string kebab.core/->kebab-case-keyword))

(s/defn json-response->key-map-token :- (s/maybe {s/Keyword s/Any})
  [response :- (s/maybe {s/Keyword s/Any})]
  (-> response
      json-response->key-map
      (update :response-content response-content->map)))

(s/defn internal->token-request :- out.authlete/TokenRequest
  [{:keys [client-id]
    :as   request} :- models.token/TokenRequest]
  (let [redirect-uri (some-> (:redirect-uri request)
                             (str/lower-case))
        parameters   (-> request
                         (dissoc :client-id)
                         (dissoc :merchant-source-id)
                         (misc/assoc-some :redirect-uri redirect-uri)
                         map->query-string)]
    (misc/assoc-some {:parameters parameters}
      :client-id client-id)))

;TODO update fingerprint and display name once we have some position of uber about where to get those
(s/defn ^:private in->oauth-authorization-details-response :- out.authorization/AuthorizationDetails
  [subject :- s/Str]
  {:type         "authorized_account"
   :account-id   subject
   :fingerprint  subject
   :display-name subject})

(s/defn in->token-response :- out.authorization/TokenResponse
  [token :- in.authlete/ResponseContent
   refresh-token-duration :- types.numeric/NonNegativeInt
   subject :- s/Str]
  (assoc (select-keys token [:expires-in
                             :access-token
                             :refresh-token
                             :scope])
    :token-type "bearer"
    :refresh-token-expires-in refresh-token-duration
    :authorization-details [(in->oauth-authorization-details-response subject)]))

(s/defn cancel-authorization-error-response->wire-out :- out.authlete/CancelErrorResponse
  [{:keys [result-message result-code]} :- in.authlete/CancelAuthorizationErrorResponse]
  {:error-message result-message
   :error-code    result-code})

(s/defn in->revoke-request :- out.authlete/RevokeRequest
  [{:keys [access-token client-id refresh-token customer-reference-key]} :- in.authorization/AuthorizationUpdateRequest]
  (misc/assoc-some {}
    :access-token-identifier access-token
    :client-identifier (parse-long client-id)
    :refresh-token-identifier refresh-token
    :subject customer-reference-key))

(s/defn in->revoke-response :- out.authorization/AuthorizationUpdateResponse
  [{:keys [authlete-response custom-response]} :- models.token/AuthorizationUpdate]
  (misc/assoc-some (select-keys authlete-response [:count
                                                   :result-message
                                                   :result-code])
    :custom-response custom-response))

(s/defn ^:private ciba-confirm-consent->complete-result :- out.authlete/CompleteResult
  [confirm-consent :- s/Bool
   validation-error :- (s/maybe s/Bool)]
  (cond
    validation-error
    "TRANSACTION_FAILED"

    confirm-consent
    "AUTHORIZED"

    (not confirm-consent)
    "ACCESS_DENIED"))

(s/defn in->ciba-complete-request :- out.authlete/CibaCompleteRequest
  [{:keys [ticket customer-id confirm-consent validation-error]} :- models.ciba/Complete]
  {:ticket  ticket
   :subject (str customer-id)
   :result  (ciba-confirm-consent->complete-result confirm-consent validation-error)})

(s/defn in->ciba-fail-request :- out.authlete/FailRequest
  [{:keys [ticket reason error-description error-uri]} :- out.authlete/FailRequest]
  (misc/assoc-some {:error-description error-description}
    :ticket ticket
    :reason reason
    :error-uri error-uri))

(s/defn ^:private regex-groups->array-pair
  [[_ k v]]
  (let [key   (-> k keyword kebab.core/->kebab-case-keyword)
        value (when (not-empty v) (util.codec/url-decode v))]
    [key value]))

(s/defn ^:private split-query-string
  [query-string :- s/Str]
  (->> query-string
       (re-seq #"(?m)(.*?)=(.*?)(&|$)")
       (map regex-groups->array-pair)))

(s/defn query-string->map :- {s/Keyword s/Any}
  [parameters :- s/Str]
  (->> (split-query-string parameters)
       (reduce (fn [acc [k v]]
                 (update acc k conj v))
               {})
       (map    (fn [[k v]]
                 [k (if (= (count v) 1) (first v) (vec v))]))
       (into   {})))

(s/defn ciba-issue-response->wire-out :- out.ciba/AuthenticationResponse
  [{:keys [result-message result-code action response-content]} :- in.authlete/IssueResponse]
  (misc/assoc-some {:result-message   result-message
                    :result-code      result-code
                    :response-content response-content}
    :action action))

(s/defn authlete-ciba-exception-info->wire-out :- out.ciba/AuthenticationResponse
  [ex-info]
  (let [{:keys [responseContent action resultCode resultMessage]} (-> ex-info :details :body)]
    (misc/assoc-some {:result-message   resultMessage
                      :result-code      resultCode
                      :response-content responseContent}
      :action action)))

(s/defn authlete-ciba-authentication-failure->wire-out :- out.ciba/AuthenticationResponse
  [fail-response :- in.authlete/FailResponse]
  (let [{:keys [response-content action result-code result-message]} fail-response]
    (misc/assoc-some {:result-message   result-message
                      :result-code      result-code
                      :response-content response-content}
      :action action)))

(s/defn success-response-content->state :- (s/maybe s/Str)
  [response-content :- s/Str]
  (-> (str/split response-content #"\?")
      last
      (query-string->map)
      (get :state)
      str
      (state-decoded)))

(s/defn in->authorization-response :- in.authlete/AuthorizationResponse
  [response :- s/Any]
  (let [{:keys [action response-content client ticket scope result-message result-code]} (json-response->key-map response)]
    (misc/assoc-some {:client client
                      :action action}
      :response-content response-content
      :ticket ticket
      :scope scope
      :result-message result-message
      :result-code result-code)))

(s/defn in->cancel-request :- out.authlete/CancelRequest
  [{:keys [reason description]} :- in.oauth2/CancelRequest
   ticket :- s/Str]
  (misc/assoc-some {:ticket ticket
                    :reason reason}
    :description description))

(s/defn in->cancel-authorization-response :- in.authlete/CancelAuthorizationResponse
  [response :- s/Any]
  (let [{:keys [action result-message result-code response-content]} (json-response->key-map response)]
    (misc/assoc-some {:result-message result-message
                      :result-code    result-code}
      :action action
      :response-content response-content)))

(s/defn wire-in->ciba-authenticate-request :- out.ciba/AuthenticationRequest
  [request :- {s/Keyword s/Any}]
  (let [scopes (get request :scope)]
    {:parameters (-> request
                     (assoc :scope (scopes->str scopes))
                     map->query-string)}))

(s/defn wire-in->ciba-issue-request :- out.ciba/IssueRequest
  [{:keys [ticket]} :- in.ciba/IssueRequest]
  {:ticket ticket})