# 080 - Event Sourcing Projection

**Level**: 16/18
**Type**: Controller
**Concepts**: Event sourcing, State projection, Event replay, Fold/reduce patterns

## Context

Event sourcing is an architectural pattern where state changes are stored as a sequence of events rather than the current state itself. The current state is rebuilt by replaying all events from the beginning. This enables time travel, auditing, and debugging. Financial systems, collaboration tools, and e-commerce platforms use event sourcing for accountability and historical analysis.

## Objective

Implement an event sourcing projection system that rebuilds entity state by replaying a sequence of domain events, handling different event types and maintaining consistency.

## Specification

### Input

- `events` (vector of maps): Sequence of domain events, each with:
  - `:event-type` (keyword): Type of event
  - `:timestamp` (number): When event occurred
  - `:data` (map): Event-specific payload
- `initial-state` (map): Starting state (optional, defaults to empty)

### Output

- (map): Final projected state containing:
  - Current entity data
  - `:version` (number): Number of events applied
  - `:last-updated` (number): Timestamp of last event

### Rules

- Events must be processed in order
- Each event type has specific state transformation logic
- Invalid events should be skipped with warning logged
- Support these event types:
  - `:account-created` - Initialize account
  - `:deposit-made` - Add to balance
  - `:withdrawal-made` - Subtract from balance
  - `:account-locked` - Set locked status
  - `:account-unlocked` - Clear locked status
  - `:interest-accrued` - Add interest to balance
- Withdrawals from insufficient balance should be rejected
- Operations on locked accounts should be rejected (except unlock)

## Examples

### Example 1: Account lifecycle
```clojure
(project-events
  [{:event-type :account-created
    :timestamp 1000
    :data {:account-id "ACC-123"
           :owner "John Doe"
           :initial-balance 0}}
   {:event-type :deposit-made
    :timestamp 1100
    :data {:amount 1000}}
   {:event-type :withdrawal-made
    :timestamp 1200
    :data {:amount 200}}]
  {})
;; => {:account-id "ACC-123"
;;     :owner "John Doe"
;;     :balance 800
;;     :status :active
;;     :version 3
;;     :last-updated 1200}
```

### Example 2: Locked account operations
```clojure
(project-events
  [{:event-type :account-created
    :timestamp 1000
    :data {:account-id "ACC-456" :owner "Jane Smith" :initial-balance 500}}
   {:event-type :account-locked
    :timestamp 1100
    :data {:reason "suspicious-activity"}}
   {:event-type :withdrawal-made
    :timestamp 1200
    :data {:amount 100}}
   {:event-type :account-unlocked
    :timestamp 1300
    :data {}}]
  {})
;; => {:account-id "ACC-456"
;;     :owner "Jane Smith"
;;     :balance 500
;;     :status :active
;;     :locked-reason nil
;;     :version 4
;;     :last-updated 1300
;;     :rejected-events [{:event-type :withdrawal-made
;;                       :reason "Account locked"}]}
```

### Example 3: Interest accrual
```clojure
(project-events
  [{:event-type :account-created
    :timestamp 1000
    :data {:account-id "ACC-789" :owner "Bob" :initial-balance 1000}}
   {:event-type :interest-accrued
    :timestamp 2000
    :data {:rate 0.05}}]
  {})
;; => {:account-id "ACC-789"
;;     :owner "Bob"
;;     :balance 1050
;;     :status :active
;;     :version 2
;;     :last-updated 2000}
```

## Tips

- Use `reduce` to fold events into state
- Create separate handler function for each event type
- Use multi-methods for event dispatch
- Maintain event version counter
- Log or collect rejected events for audit
- Consider immutability - each event creates new state

## Testing your solution

```bash
cd challenges/080-event-sourcing-projection/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-080.solution)
(challenge-080.solution/-test)
```
