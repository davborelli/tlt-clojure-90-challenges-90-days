# 088 - Complex Service Orchestration

**Level**: 18/18
**Type**: Controller
**Concepts**: Microservices orchestration, Circuit breaker, Saga pattern, Compensation, Distributed tracing

## Context

Microservices architectures require orchestrating multiple services to complete business transactions. Complex orchestrations must handle: partial failures, service timeouts, circuit breakers to prevent cascade failures, compensating transactions for rollback, and distributed tracing for debugging. This is essential for e-commerce checkouts, financial transactions, and multi-step workflows.

## Objective

Implement a sophisticated service orchestrator that coordinates multiple microservices with circuit breakers, compensation logic, retry policies, and comprehensive error handling.

## Specification

### Input

- `orchestration-plan` (map): Plan with:
  - `:steps` (vector): Sequential service calls
  - `:circuit-breakers` (map): Circuit breaker config per service
  - `:compensation` (map): Compensation functions for rollback
  - `:retry-policy` (map): Retry configuration
- `initial-context` (map): Starting context/data

### Output

- (map): Orchestration result with:
  - `:status` (keyword): :success, :partial-failure, :failure
  - `:results` (vector): Results from each step
  - `:compensations-executed` (vector): Compensations run (if failed)
  - `:trace` (vector): Execution trace for debugging
  - `:circuit-breaker-states` (map): State of circuit breakers

### Rules

- Execute steps sequentially, passing context between steps
- Apply circuit breakers to prevent cascade failures
- On failure, execute compensation functions in reverse order
- Implement retry with exponential backoff
- Track distributed trace IDs across services
- Handle timeouts gracefully
- Support parallel execution for independent steps

## Examples

### Example 1: Successful orchestration
```clojure
(orchestrate
  {:steps [{:service :inventory :action :reserve}
           {:service :payment :action :charge}
           {:service :shipping :action :schedule}]
   :circuit-breakers {:inventory {:threshold 5 :timeout 30000}}}
  {:order-id "ORD-123" :items [...] :total 100})
;; => {:status :success
;;     :results [{:service :inventory :result {:reserved true}}
;;               {:service :payment :result {:charged true :tx-id "PAY-456"}}
;;               {:service :shipping :result {:scheduled true :tracking "SHIP-789"}}]
;;     :trace [{:step 1 :service :inventory :duration-ms 50}
;;             {:step 2 :service :payment :duration-ms 150}
;;             {:step 3 :service :shipping :duration-ms 80}]}
```

### Example 2: Failure with compensation (Saga pattern)
```clojure
(orchestrate
  {:steps [{:service :inventory :action :reserve}
           {:service :payment :action :charge}
           {:service :shipping :action :schedule}]
   :compensation {:inventory #(unreserve %)
                 :payment #(refund %)}}
  {:order-id "ORD-456" :items [...]})
;; Payment fails, triggers compensation:
;; => {:status :failure
;;     :results [{:service :inventory :result {:reserved true}}
;;               {:service :payment :error "Card declined"}]
;;     :compensations-executed [{:service :inventory :action :unreserve :result {:released true}}]
;;     :final-state :rolled-back}
```

## Tips

- Implement circuit breaker with half-open state
- Use correlation IDs for distributed tracing
- Log extensively for debugging distributed issues
- Consider idempotency for retry safety
- Use timeouts to prevent hanging
- Implement bulkhead pattern for resource isolation

## Testing your solution

```bash
cd challenges/088-complex-orchestration/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-088.solution)
(challenge-088.solution/-test)
```
