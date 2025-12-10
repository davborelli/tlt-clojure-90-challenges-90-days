;; =============================================================================
;; 087 - MULTI-PROTOCOL ADAPTER
;; Level: 18/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Modern microservices architectures require supporting multiple communication
;; protocols simultaneously. REST/HTTP is ubiquitous for web clients and public
;; APIs, gRPC provides high-performance binary communication for inter-service
;; calls, GraphQL offers flexible querying for frontend applications, and each
;; has distinct conventions for status codes, error handling, and data formats.
;;
;; This solution implements a unified adapter that abstracts protocol-specific
;; concerns behind a consistent interface. The core insight is that while
;; protocols differ in their wire formats and semantics, the underlying business
;; logic remains the same. By separating protocol concerns from business logic,
;; we achieve several benefits: services can support multiple protocols without
;; code duplication, new protocols can be added without changing core logic,
;; and protocol-specific optimizations (like content negotiation) are centralized.
;;
;; The implementation uses multimethods for protocol dispatch, enabling clean
;; extensibility. Each protocol has its own response formatter that handles
;; protocol-specific concerns like HTTP status codes (200, 404, 500), gRPC
;; status codes (OK, NOT_FOUND, INTERNAL), and GraphQL conventions (always
;; HTTP 200 with errors in the response body). The adapter also handles content
;; negotiation, choosing appropriate serialization formats (JSON for HTTP/GraphQL,
;; Protocol Buffers for gRPC), and preserves trace IDs for distributed debugging.
;;
;; This pattern is essential in production systems where API gateways must
;; translate between protocols, service meshes need to route different protocol
;; types, and backend services want to support multiple client types without
;; maintaining separate implementations. The unified interface approach ensures
;; consistent behavior across protocols while respecting each protocol's idioms.

(ns challenge-087.solution
  (:require [clojure.string :as str]))

;; IMPLEMENTATION
;; --------------

;; Protocol-specific status code mappings

(def http-status-codes
  "Maps unified status to HTTP status codes following REST conventions"
  {:success    200
   :created    201
   :no-content 204
   :not-found  404
   :bad-request 400
   :unauthorized 401
   :forbidden  403
   :error      500
   :timeout    504})

(def grpc-status-codes
  "Maps unified status to gRPC status codes"
  {:success    "OK"
   :not-found  "NOT_FOUND"
   :bad-request "INVALID_ARGUMENT"
   :unauthorized "UNAUTHENTICATED"
   :forbidden  "PERMISSION_DENIED"
   :error      "INTERNAL"
   :timeout    "DEADLINE_EXCEEDED"
   :unavailable "UNAVAILABLE"})

(defn extract-trace-id
  "Extracts trace ID from request headers for distributed tracing"
  [headers]
  (or (get headers "X-Trace-Id")
      (get headers "x-trace-id")
      (get headers "traceparent")
      (str (java.util.UUID/randomUUID))))

(defn format-http-response
  "Formats response according to HTTP/REST conventions.

  HTTP uses status codes to indicate request outcome (2xx success, 4xx client
  error, 5xx server error). Errors include both status code and descriptive
  message in response body. Content-Type negotiation determines JSON format."
  [status data headers]
  (let [http-status (get http-status-codes status 500)
        trace-id (extract-trace-id headers)]
    {:status status
     :data data
     :protocol-specific {:http-status http-status
                        :content-type "application/json"
                        :trace-id trace-id}
     :headers {"Content-Type" "application/json"
               "X-Trace-Id" trace-id}}))

(defn format-grpc-response
  "Formats response according to gRPC conventions.

  gRPC uses specific status codes (OK, NOT_FOUND, etc.) and Protocol Buffers
  for efficient binary serialization. All responses include grpc-status in
  metadata, with error details in grpc-message when applicable."
  [status data headers]
  (let [grpc-code (get grpc-status-codes status "INTERNAL")
        trace-id (extract-trace-id headers)]
    {:status status
     :data data
     :protocol-specific {:grpc-code grpc-code
                        :encoding "protobuf"
                        :trace-id trace-id}
     :headers {"grpc-status" (if (= status :success) "0" "2")
               "grpc-message" (when (not= status :success)
                               (str "Error: " (name status)))
               "trace-id" trace-id}}))

(defn format-graphql-response
  "Formats response according to GraphQL conventions.

  GraphQL always returns HTTP 200, placing errors in the 'errors' array within
  the response body. Successful operations return data in the 'data' field.
  This allows partial success: some fields may succeed while others error."
  [status data headers]
  (let [trace-id (extract-trace-id headers)
        success? (= status :success)]
    {:status (if success? :success :error)
     :data (if success?
            {:data data}
            {:data nil
             :errors [{:message (str "Operation failed: " (name status))
                      :extensions {:code (str/upper-case (name status))
                                  :trace-id trace-id}}]})
     :protocol-specific {:http-status 200  ; GraphQL always returns 200
                        :trace-id trace-id}
     :headers {"Content-Type" "application/json"
               "X-Trace-Id" trace-id}}))

;; Multimethod for protocol dispatch

(defmulti format-protocol-response
  "Dispatches to protocol-specific formatter based on :protocol key.

  Using multimethods provides clean extensibility - new protocols can be
  added by implementing new format-protocol-response methods without
  modifying existing code."
  (fn [protocol _ _ _] protocol))

(defmethod format-protocol-response :http
  [_ status data headers]
  (format-http-response status data headers))

(defmethod format-protocol-response :grpc
  [_ status data headers]
  (format-grpc-response status data headers))

(defmethod format-protocol-response :graphql
  [_ status data headers]
  (format-graphql-response status data headers))

(defmethod format-protocol-response :default
  [protocol _ _ _]
  {:status :error
   :data {:error "Unsupported protocol"}
   :protocol-specific {:error (str "Protocol not supported: " protocol)}})

(defn execute-with-error-handling
  "Executes service function with unified error handling.

  Catches exceptions and translates them to appropriate status codes.
  This abstraction allows business logic to throw exceptions without
  worrying about protocol-specific error formats."
  [service-fn params]
  (try
    (let [result (service-fn params)]
      (if (and (map? result) (:error result))
        {:status (or (:status result) :error)
         :data result}
        {:status :success
         :data result}))
    (catch IllegalArgumentException e
      {:status :bad-request
       :data {:error (.getMessage e)}})
    (catch SecurityException e
      {:status :unauthorized
       :data {:error (.getMessage e)}})
    (catch clojure.lang.ExceptionInfo e
      (let [ex-data (ex-data e)]
        {:status (or (:status ex-data) :error)
         :data {:error (.getMessage e)
                :details ex-data}}))
    (catch Exception e
      {:status :error
       :data {:error "Internal server error"
              :message (.getMessage e)}})))

(defn validate-request
  "Validates request structure and required fields.

  Returns nil if valid, error map if invalid. This provides early validation
  before executing business logic, saving resources on malformed requests."
  [request]
  (cond
    (not (map? request))
    {:status :bad-request
     :data {:error "Request must be a map"}}

    (not (:protocol request))
    {:status :bad-request
     :data {:error "Protocol is required"}}

    (not (#{:http :grpc :graphql} (:protocol request)))
    {:status :bad-request
     :data {:error "Protocol must be :http, :grpc, or :graphql"}}

    (not (:operation request))
    {:status :bad-request
     :data {:error "Operation is required"}}

    :else nil))

(defn adapt-protocol
  "Unified multi-protocol adapter.

  Parameters:
  - request: Map with :protocol, :operation, :params, :headers
  - service-fn: Business logic function to execute

  Returns: Unified response with protocol-specific formatting

  This is the main entry point that orchestrates the entire adaptation process:
  1. Validates request structure
  2. Executes business logic with error handling
  3. Formats response according to protocol conventions
  4. Adds protocol-specific metadata and headers"
  [request service-fn]
  (let [validation-error (validate-request request)]
    (if validation-error
      ;; Return validation error formatted for the requested protocol
      (format-protocol-response
        (or (:protocol request) :http)
        (:status validation-error)
        (:data validation-error)
        (or (:headers request) {}))

      ;; Execute business logic and format response
      (let [{:keys [protocol params headers]} request
            headers (or headers {})
            {:keys [status data]} (execute-with-error-handling service-fn params)]
        (format-protocol-response protocol status data headers)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Protocol Abstraction
;;    Protocol abstraction separates business logic from communication protocol
;;    concerns. The business logic operates on domain objects and returns results,
;;    while adapters handle protocol-specific serialization, status codes, and
;;    error formats. This separation enables supporting multiple protocols without
;;    duplicating business logic. In production systems, this pattern is crucial
;;    for API gateways, service meshes, and polyglot architectures where services
;;    must speak multiple protocols. The key insight is that protocols are an
;;    infrastructure concern, not a business concern, so they should be handled
;;    at the boundaries of the system.
;;
;; 2. Multimethods for Extensibility
;;    Multimethods provide open-closed extensibility: new protocols can be added
;;    by implementing new methods without modifying existing code. This is superior
;;    to case statements because protocols can be defined in separate namespaces
;;    or even external libraries. The dispatch function (:protocol key) determines
;;    which method to invoke. For production systems with many protocols, consider
;;    using protocols (the Clojure feature) instead of multimethods for better
;;    performance, but multimethods provide clearer dispatch logic for this use case.
;;
;; 3. Content Negotiation
;;    Content negotiation allows clients and servers to agree on data formats
;;    (JSON, Protocol Buffers, XML). HTTP uses Accept headers, gRPC specifies
;;    format in the protocol, GraphQL always uses JSON. This implementation handles
;;    basic content negotiation by mapping protocols to their standard formats.
;;    In production, you'd parse Accept headers, support multiple formats per
;;    protocol, and handle quality values (q-factors) for preference ordering.
;;    Libraries like Ring's content negotiation middleware can help with this.
;;
;; 4. Error Translation
;;    Each protocol has its own conventions for representing errors. HTTP uses
;;    status codes (4xx for client errors, 5xx for server errors), gRPC has
;;    specific status codes (INVALID_ARGUMENT, INTERNAL, etc.), and GraphQL
;;    places errors in the response body while keeping HTTP 200. This implementation
;;    translates unified error statuses to protocol-specific formats. Production
;;    systems should also translate exception types (IllegalArgumentException →
;;    bad request, NullPointerException → internal error) and preserve error
;;    context like validation failures across protocols.
;;
;; 5. Distributed Tracing
;;    Distributed tracing tracks requests across service boundaries using trace
;;    IDs. This implementation extracts trace IDs from standard headers
;;    (X-Trace-Id, traceparent) and propagates them through responses. In production,
;;    use OpenTelemetry or similar frameworks that handle trace context propagation,
;;    span creation, and integration with tracing backends like Jaeger or Zipkin.
;;    Trace IDs are essential for debugging distributed systems where a single
;;    user request touches multiple services and protocols.
;;
;; 6. Unified Status Codes
;;    This implementation defines a unified set of status codes (:success,
;;    :error, :not-found, etc.) that map to protocol-specific codes. This
;;    abstraction allows business logic to return generic statuses without knowing
;;    the target protocol. The adapter translates these to HTTP status codes
;;    (200, 404, 500), gRPC codes (OK, NOT_FOUND, INTERNAL), or GraphQL error
;;    structures. In production, you might use a more sophisticated status
;;    taxonomy that captures business-level errors (insufficient_funds,
;;    duplicate_entry) separately from technical errors (timeout, unavailable).
;;
;; 7. Protocol-Specific Conventions
;;    Each protocol has idiomatic patterns that should be respected. HTTP RESTful
;;    APIs use appropriate status codes (201 for creation, 204 for no content),
;;    gRPC returns detailed error messages in metadata, GraphQL supports partial
;;    success with some fields succeeding and others failing. This implementation
;;    captures these conventions in the format functions. When adding new protocols,
;;    study their conventions: WebSockets use different frame types, message queues
;;    use acknowledgments and dead-letter queues, and RPC systems may have custom
;;    retry policies. Respecting protocol conventions makes your services easier
;;    to integrate with standard tooling and familiar to developers.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo3.md
;;
;; Pattern used: Multi-format adapter with query string parsing and case conversion
;;
;; The reference code demonstrates comprehensive adapter patterns:
;;
;; 1. Key transformation (->camelCase):
;;    (defn ->camelCase [payload]
;;      (update-keys payload kebab.core/->camelCaseKeyword))
;;
;;    This shows systematic key renaming for protocol compatibility, similar to
;;    how our solution transforms data structures for different protocols.
;;
;; 2. Query string parsing (query-string->map):
;;    The reference shows complex parsing of URL-encoded query strings with
;;    regex-based extraction and proper URL decoding. This is analogous to our
;;    protocol-specific parsing where each protocol has its own serialization format.
;;
;; 3. Bidirectional transformations:
;;    map->query-string and query-string->map show bidirectional adapters,
;;    similar to how our solution formats responses differently per protocol
;;    but accepts unified input.
;;
;; 4. Status code mapping:
;;    (def status-codes {:INTERNAL_SERVER_ERROR 500 :OK 200})
;;    This direct mapping pattern is used in our solution for both HTTP and
;;    gRPC status codes.
;;
;; Real-world usage: In the Authlete OAuth adapter, multiple wire formats (JSON,
;; query strings, URL-encoded) are transformed to/from domain models. The adapter
;; handles protocol-specific concerns (scope formatting, authorization details
;; encoding) while keeping business logic clean. This is exactly the problem our
;; multi-protocol adapter solves at a higher level of abstraction.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: HTTP REST request - Success
  (def get-user-fn
    (fn [params]
      {:user-id (:user-id params)
       :name "John Doe"
       :email "john@example.com"}))

  (adapt-protocol
    {:protocol :http
     :operation :get-user
     :params {:user-id "123"}
     :headers {"Accept" "application/json"
               "X-Trace-Id" "trace-123"}}
    get-user-fn)
  ;; => {:status :success
  ;;     :data {:user-id "123" :name "John Doe" :email "john@example.com"}
  ;;     :protocol-specific {:http-status 200
  ;;                        :content-type "application/json"
  ;;                        :trace-id "trace-123"}
  ;;     :headers {"Content-Type" "application/json"
  ;;               "X-Trace-Id" "trace-123"}}

  ;; Example 2: gRPC request - Success with efficient binary format
  (adapt-protocol
    {:protocol :grpc
     :operation :get-user
     :params {:user-id "456"}
     :headers {"trace-id" "grpc-trace-456"}}
    get-user-fn)
  ;; => {:status :success
  ;;     :data {:user-id "456" :name "John Doe" :email "john@example.com"}
  ;;     :protocol-specific {:grpc-code "OK"
  ;;                        :encoding "protobuf"
  ;;                        :trace-id "grpc-trace-456"}
  ;;     :headers {"grpc-status" "0"
  ;;               "grpc-message" nil
  ;;               "trace-id" "grpc-trace-456"}}

  ;; Example 3: GraphQL query - Successful nested query
  (def execute-graphql-fn
    (fn [params]
      {:user {:name "Bob Smith"
              :email "bob@example.com"
              :address {:city "New York" :country "USA"}}}))

  (adapt-protocol
    {:protocol :graphql
     :operation :query
     :params {:query "{ user(id: \"789\") { name email address { city } } }"}
     :headers {"X-Trace-Id" "gql-trace-789"}}
    execute-graphql-fn)
  ;; => {:status :success
  ;;     :data {:data {:user {:name "Bob Smith"
  ;;                         :email "bob@example.com"
  ;;                         :address {:city "New York" :country "USA"}}}}
  ;;     :protocol-specific {:http-status 200  ; GraphQL always 200
  ;;                        :trace-id "gql-trace-789"}
  ;;     :headers {"Content-Type" "application/json"
  ;;               "X-Trace-Id" "gql-trace-789"}}

  ;; Example 4: HTTP error - Not found
  (def get-missing-user-fn
    (fn [params]
      (throw (ex-info "User not found"
                      {:status :not-found
                       :user-id (:user-id params)}))))

  (adapt-protocol
    {:protocol :http
     :operation :get-user
     :params {:user-id "999"}
     :headers {}}
    get-missing-user-fn)
  ;; => {:status :not-found
  ;;     :data {:error "User not found"
  ;;            :details {:status :not-found :user-id "999"}}
  ;;     :protocol-specific {:http-status 404 ...}
  ;;     :headers {"Content-Type" "application/json" ...}}

  ;; Example 5: gRPC error - Invalid argument
  (def invalid-request-fn
    (fn [params]
      (throw (IllegalArgumentException. "User ID must be numeric"))))

  (adapt-protocol
    {:protocol :grpc
     :operation :get-user
     :params {:user-id "invalid"}
     :headers {}}
    invalid-request-fn)
  ;; => {:status :bad-request
  ;;     :data {:error "User ID must be numeric"}
  ;;     :protocol-specific {:grpc-code "INVALID_ARGUMENT" ...}
  ;;     :headers {"grpc-status" "2"
  ;;               "grpc-message" "Error: bad-request" ...}}

  ;; Example 6: GraphQL error - Still returns HTTP 200
  (def failing-fn
    (fn [params]
      (throw (SecurityException. "Unauthorized access"))))

  (adapt-protocol
    {:protocol :graphql
     :operation :mutation
     :params {:action "delete"}
     :headers {}}
    failing-fn)
  ;; => {:status :error
  ;;     :data {:data nil
  ;;            :errors [{:message "Operation failed: unauthorized"
  ;;                     :extensions {:code "UNAUTHORIZED" ...}}]}
  ;;     :protocol-specific {:http-status 200  ; GraphQL convention!
  ;;                        :trace-id ...}
  ;;     :headers {"Content-Type" "application/json" ...}}

  ;; Example 7: Validation error - Caught before execution
  (adapt-protocol
    {:protocol :unknown-protocol
     :operation :test}
    get-user-fn)
  ;; => {:status :bad-request
  ;;     :data {:error "Protocol must be :http, :grpc, or :graphql"}
  ;;     :protocol-specific {:error "Protocol not supported: :unknown-protocol"}
  ;;     :headers {}}
)

;; TESTS
;; -----

(defn -test []
  ;; Test 1: HTTP success
  (let [result (adapt-protocol
                 {:protocol :http
                  :operation :test
                  :params {:id 1}
                  :headers {"X-Trace-Id" "test-1"}}
                 (fn [_] {:success true}))]
    (assert (= (:status result) :success)
            "HTTP request should succeed")
    (assert (= (get-in result [:protocol-specific :http-status]) 200)
            "HTTP should return 200 status")
    (assert (= (get-in result [:headers "Content-Type"]) "application/json")
            "HTTP should set JSON content type"))

  ;; Test 2: gRPC success
  (let [result (adapt-protocol
                 {:protocol :grpc
                  :operation :test
                  :params {:id 2}
                  :headers {}}
                 (fn [_] {:data "test"}))]
    (assert (= (:status result) :success)
            "gRPC request should succeed")
    (assert (= (get-in result [:protocol-specific :grpc-code]) "OK")
            "gRPC should return OK code")
    (assert (= (get-in result [:protocol-specific :encoding]) "protobuf")
            "gRPC should use protobuf encoding"))

  ;; Test 3: GraphQL success - always HTTP 200
  (let [result (adapt-protocol
                 {:protocol :graphql
                  :operation :query
                  :params {:query "{ test }"}
                  :headers {}}
                 (fn [_] {:result "success"}))]
    (assert (= (:status result) :success)
            "GraphQL query should succeed")
    (assert (= (get-in result [:protocol-specific :http-status]) 200)
            "GraphQL should always return HTTP 200")
    (assert (contains? (:data result) :data)
            "GraphQL should wrap result in :data"))

  ;; Test 4: HTTP error handling
  (let [result (adapt-protocol
                 {:protocol :http
                  :operation :test
                  :params {:id "invalid"}
                  :headers {}}
                 (fn [_] (throw (IllegalArgumentException. "Invalid ID"))))]
    (assert (= (:status result) :bad-request)
            "Should catch IllegalArgumentException as bad-request")
    (assert (= (get-in result [:protocol-specific :http-status]) 400)
            "HTTP should return 400 for bad request"))

  ;; Test 5: GraphQL error - HTTP 200 with errors array
  (let [result (adapt-protocol
                 {:protocol :graphql
                  :operation :mutation
                  :params {:action "delete"}
                  :headers {}}
                 (fn [_] (throw (SecurityException. "Unauthorized"))))]
    (assert (= (:status result) :error)
            "Should catch SecurityException as error")
    (assert (= (get-in result [:protocol-specific :http-status]) 200)
            "GraphQL should return HTTP 200 even for errors")
    (assert (contains? (:data result) :errors)
            "GraphQL should include errors array"))

  ;; Test 6: Validation error - missing protocol
  (let [result (adapt-protocol
                 {:operation :test
                  :params {}}
                 (fn [_] {:result "should not execute"}))]
    (assert (= (:status result) :bad-request)
            "Should validate required protocol field")
    (assert (str/includes? (get-in result [:data :error]) "Protocol")
            "Error message should mention protocol"))

  ;; Test 7: Trace ID propagation
  (let [trace-id "test-trace-123"
        result (adapt-protocol
                 {:protocol :http
                  :operation :test
                  :params {}
                  :headers {"X-Trace-Id" trace-id}}
                 (fn [_] {:ok true}))]
    (assert (= (get-in result [:headers "X-Trace-Id"]) trace-id)
            "Should propagate trace ID through response")
    (assert (= (get-in result [:protocol-specific :trace-id]) trace-id)
            "Should include trace ID in protocol-specific metadata"))

  (println "✓ All tests passed! The multi-protocol adapter works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
