# 083 - Advanced State Machine

**Level**: 17/18
**Type**: Controller
**Concepts**: State machines, Guards, Side effects, State history, Transition validation

## Context

State machines are fundamental to workflow systems, game engines, protocol implementations, and business process automation. An advanced state machine goes beyond simple state transitions to include: guard conditions (transition only if condition met), side effects (actions on transition), state history (for debugging and auditing), and complex transition validation.

## Objective

Implement an advanced state machine that manages workflow states with guard conditions, side effects, history tracking, and comprehensive transition validation.

## Specification

### Input

- `machine-state` (map): Current state machine with:
  - `:current-state` (keyword): Current state
  - `:context` (map): Shared data context
  - `:history` (vector): State transition history
  - `:definition` (map): State machine definition with transitions, guards, effects
- `event` (keyword): Event triggering transition
- `event-data` (map): Data associated with event

### Output

- (map): New machine state or error with:
  - `:current-state`: New state (if transition successful)
  - `:context`: Updated context
  - `:history`: Updated history with transition record
  - `:effects`: List of side effects to execute
  - `:error` (if failed): Reason for transition failure

### Rules

- Only defined transitions are allowed
- Guards must pass for transition to proceed
- Effects are collected but not executed (pure function)
- History records: from-state, to-state, event, timestamp, context snapshot
- Context can be updated during transitions
- Support wildcard transitions (from any state)

## Examples

### Example 1: Simple transition
```clojure
(process-event
  {:current-state :draft
   :context {:doc-id "DOC-123" :version 1}
   :history []
   :definition {:transitions {:draft {:submit :review}}}}
  :submit
  {})
;; => {:current-state :review
;;     :context {:doc-id "DOC-123" :version 1}
;;     :history [{:from :draft :to :review :event :submit :timestamp ...}]
;;     :effects []}
```

### Example 2: Transition with guard
```clojure
(process-event
  {:current-state :review
   :context {:doc-id "DOC-123" :approvals 1 :required-approvals 2}
   :history [...]
   :definition {:transitions {:review {:approve :approved}}
                :guards {:review {:approve (fn [ctx _] (>= (:approvals ctx) (:required-approvals ctx)))}}}}
  :approve
  {})
;; => {:error "Guard condition failed for transition :review -> :approve"
;;     :current-state :review  ; State unchanged
;;     ...}
```

### Example 3: Transition with side effects
```clojure
(process-event
  {:current-state :review
   :context {:doc-id "DOC-123"}
   :history []
   :definition {:transitions {:review {:publish :published}}
                :effects {:review {:publish [:send-notification :update-index]}}}}
  :publish
  {})
;; => {:current-state :published
;;     :effects [:send-notification :update-index]
;;     ...}
```

## Tips

- Use multi-methods for extensible guard/effect definitions
- Keep state machine pure - effects are descriptors, not actions
- Consider using a DSL for state machine definitions
- Validate state machine definition on creation
- Use namespaced keywords for states to avoid conflicts
- Track transition metadata for debugging

## Testing your solution

```bash
cd challenges/083-advanced-state-machine/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-083.solution)
(challenge-083.solution/-test)
```
