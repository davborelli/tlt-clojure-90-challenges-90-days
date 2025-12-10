# 066 - State Machine Transition

**Level**: 14/18
**Type**: Pure Function
**Concepts**: State machines, Complex pattern matching, State transitions, Guards

## Context

State machines model workflows with defined states and transitions. An order lifecycle might progress: pending → confirmed → shipped → delivered. Not all transitions are valid (can't ship before confirming), and some transitions have additional conditions (guards).

## Objective

Implement a pure function that determines the next state in an order state machine, validating transitions and checking guard conditions.

## Specification

### Input

- `current-state` (keyword): Current state
- `event` (keyword): Triggering event
- `context` (map): Additional data for guards
  ```clojure
  {:payment-confirmed boolean
   :items-in-stock boolean
   :address-valid boolean}
  ```

### Output

- (keyword): Next state, or `:invalid-transition` if transition not allowed

### Rules

**Valid transitions:**

From `:pending`:
- Event `:confirm` + payment confirmed + items in stock → `:confirmed`
- Event `:cancel` → `:cancelled`

From `:confirmed`:
- Event `:ship` + address valid → `:shipped`
- Event `:cancel` → `:cancelled`

From `:shipped`:
- Event `:deliver` → `:delivered`
- Cannot cancel after shipping

From `:delivered`:
- No further transitions allowed

From `:cancelled`:
- No further transitions allowed

**Invalid:**
- Any undefined state/event combination → `:invalid-transition`
- Transition with failed guards → `:invalid-transition`

## Examples

### Example 1
```clojure
(next-state :pending :confirm {:payment-confirmed true :items-in-stock true :address-valid true})
;; => :confirmed
```

### Example 2
```clojure
(next-state :pending :confirm {:payment-confirmed false :items-in-stock true :address-valid true})
;; => :invalid-transition  ; Payment not confirmed
```

### Example 3
```clojure
(next-state :confirmed :ship {:payment-confirmed true :items-in-stock true :address-valid true})
;; => :shipped
```

### Example 4
```clojure
(next-state :shipped :cancel {:payment-confirmed true :items-in-stock true :address-valid true})
;; => :invalid-transition  ; Cannot cancel after shipping
```

## Tips

- Use `cond` or nested `case` for state/event combinations
- Pattern: `(and (= state :pending) (= event :confirm))` for each combo
- Check guards after matching state/event
- Return `:invalid-transition` as default
- Guards use context: `(:payment-confirmed context)`

## Testing your solution

```bash
cd challenges/066-state-machine-transition/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-066.solution)
(challenge-066.solution/-test)
```
