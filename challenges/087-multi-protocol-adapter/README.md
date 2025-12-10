# 087 - Multi-Protocol Adapter

**Level**: 18/18
**Type**: Adapter
**Concepts**: Protocol abstraction, HTTP/gRPC/GraphQL, Unified interface, Content negotiation

## Context

Modern microservices architectures support multiple communication protocols: REST/HTTP for web clients, gRPC for high-performance inter-service communication, GraphQL for flexible queries, and WebSockets for real-time updates. A multi-protocol adapter provides a unified interface, translating between protocols while preserving semantics.

## Objective

Implement a unified adapter that translates between HTTP REST, gRPC-style binary protocols, and GraphQL query structures, handling protocol-specific concerns like content negotiation, status codes, and error formats.

## Specification

### Input

- `request` (map): Unified request with:
  - `:protocol` (keyword): :http, :grpc, or :graphql
  - `:operation` (keyword): Operation to perform
  - `:params` (map): Parameters
  - `:headers` (map): Protocol-specific headers
- `service-fn` (function): Business logic function

### Output

- (map): Unified response with:
  - `:status` (keyword): :success, :error, :not-found
  - `:data`: Response data
  - `:protocol-specific`: Protocol-specific formatting (HTTP status codes, gRPC codes, GraphQL errors)
  - `:headers`: Response headers

### Rules

- HTTP: Use standard status codes (200, 404, 500), support REST semantics (GET/POST/PUT/DELETE)
- gRPC: Use gRPC status codes (OK, NOT_FOUND, INTERNAL), binary-efficient format
- GraphQL: Always return 200, errors in errors array, support nested queries
- Content negotiation: JSON for HTTP/GraphQL, Protocol Buffers for gRPC
- Error translation: Map errors appropriately per protocol
- Preserve trace IDs across protocols

## Examples

### Example 1: HTTP REST request
```clojure
(adapt-protocol
  {:protocol :http
   :operation :get-user
   :params {:user-id "123"}
   :headers {"Accept" "application/json"}}
  get-user-fn)
;; => {:status :success
;;     :data {:user-id "123" :name "John"}
;;     :protocol-specific {:http-status 200
;;                        :content-type "application/json"}}
```

### Example 2: gRPC request
```clojure
(adapt-protocol
  {:protocol :grpc
   :operation :get-user
   :params {:user-id "456"}}
  get-user-fn)
;; => {:status :success
;;     :data {:user-id "456" :name "Jane"}
;;     :protocol-specific {:grpc-code "OK"
;;                        :encoding "protobuf"}}
```

### Example 3: GraphQL query
```clojure
(adapt-protocol
  {:protocol :graphql
   :operation :query
   :params {:query "{ user(id: \"789\") { name email } }"}}
  execute-graphql-fn)
;; => {:status :success
;;     :data {:user {:name "Bob" :email "bob@example.com"}}
;;     :protocol-specific {:http-status 200}}  ; GraphQL always 200
```

## Tips

- Use protocols or multimethods for extensibility
- Handle content negotiation (JSON, Protobuf, GraphQL)
- Map errors appropriately for each protocol
- Consider rate limiting per protocol
- Implement circuit breakers for downstream calls
- Log protocol-specific metrics

## Testing your solution

```bash
cd challenges/087-multi-protocol-adapter/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-087.solution)
(challenge-087.solution/-test)
```
