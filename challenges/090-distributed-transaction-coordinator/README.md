# 090 - Distributed Transaction Coordinator (2PC)

**Level**: 18/18
**Type**: Controller
**Concepts**: Two-Phase Commit, Distributed transactions, ACID properties, Coordinator pattern, Failure recovery

## Context

Distributed transactions coordinate multiple databases or services to maintain ACID properties across boundaries. The Two-Phase Commit (2PC) protocol is the classic solution: Phase 1 (Prepare) asks all participants if they can commit, Phase 2 (Commit/Abort) tells them to proceed or rollback. This is crucial for financial systems, distributed databases, and multi-service transactions requiring atomicity.

## Objective

Implement a Two-Phase Commit coordinator that manages distributed transactions across multiple participants, handles failures, supports timeout recovery, and maintains transaction logs for durability.

## Specification

### Input

- `transaction` (map): Transaction details with:
  - `:id` (string): Unique transaction ID
  - `:participants` (vector): Services participating in transaction
  - `:operations` (vector): Operations per participant
  - `:timeout-ms` (number): Timeout for each phase
- `participant-responses` (map): Simulated responses from participants

### Output

- (map): Transaction result with:
  - `:status` (keyword): :committed, :aborted, :timeout, :partial-failure
  - `:phase-1-results` (map): Prepare phase results per participant
  - `:phase-2-results` (map): Commit/abort phase results per participant
  - `:log` (vector): Transaction log entries
  - `:recovery-actions` (vector): Actions needed if coordinator crashes

### Rules

- Phase 1 (Prepare): Ask all participants if they can commit
- All must vote YES to proceed to Phase 2
- Any NO vote triggers global abort
- Phase 2 (Commit/Abort): Tell all participants the decision
- Log decisions before sending (for recovery)
- Handle participant failures gracefully
- Support timeout and retry
- Maintain transaction state for recovery
- Ensure atomicity: all commit or all abort

## Examples

### Example 1: Successful 2PC
```clojure
(coordinate-2pc
  {:id "TXN-123"
   :participants [:db-orders :db-inventory :db-payment]
   :operations {:db-orders {:action :insert :data {...}}
                :db-inventory {:action :decrement :data {...}}
                :db-payment {:action :charge :data {...}}}
   :timeout-ms 5000}
  {:db-orders {:prepare :yes :commit :success}
   :db-inventory {:prepare :yes :commit :success}
   :db-payment {:prepare :yes :commit :success}})
;; => {:status :committed
;;     :phase-1-results {:db-orders :yes :db-inventory :yes :db-payment :yes}
;;     :phase-2-results {:db-orders :success :db-inventory :success :db-payment :success}
;;     :log [{:phase :prepare :decision :can-commit}
;;           {:phase :commit :decision :commit-all :participants [...]}}
```

### Example 2: Abort due to one NO vote
```clojure
(coordinate-2pc
  {:id "TXN-456"
   :participants [:db-orders :db-inventory :db-payment]
   :operations {...}
   :timeout-ms 5000}
  {:db-orders {:prepare :yes}
   :db-inventory {:prepare :no :reason "Insufficient stock"}
   :db-payment {:prepare :yes}})
;; => {:status :aborted
;;     :phase-1-results {:db-orders :yes :db-inventory :no :db-payment :yes}
;;     :phase-2-results {:db-orders :aborted :db-inventory :aborted :db-payment :aborted}
;;     :abort-reason "db-inventory voted NO: Insufficient stock"
;;     :log [{:phase :prepare :decision :must-abort :reason "..."}
;;           {:phase :abort :decision :abort-all}]}
```

### Example 3: Timeout recovery
```clojure
(coordinate-2pc
  {:id "TXN-789"
   :participants [:db-orders :db-payment]
   :operations {...}
   :timeout-ms 1000}
  {:db-orders {:prepare :yes :delay-ms 2000}  ; Timeout
   :db-payment {:prepare :yes}})
;; => {:status :aborted
;;     :phase-1-results {:db-orders :timeout :db-payment :yes}
;;     :phase-2-results {:db-orders :aborted :db-payment :aborted}
;;     :recovery-actions ["Retry abort for db-orders"
;;                       "Check db-orders state manually"]}
```

## Tips

- Log decisions durably before sending to participants
- Use correlation IDs for distributed tracing
- Implement timeout for each phase
- Handle coordinator crash recovery with logs
- Consider 3PC (Three-Phase Commit) for better availability
- In production, use distributed transaction managers (XA)

## Testing your solution

```bash
cd challenges/090-distributed-transaction-coordinator/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-090.solution)
(challenge-090.solution/-test)
```
