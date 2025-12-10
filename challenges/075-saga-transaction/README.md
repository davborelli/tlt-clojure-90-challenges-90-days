# 075 - Saga Transaction

**Level**: 15/18
**Type**: Controller
**Concepts**: Saga pattern, Compensating transactions, Distributed workflows, Error recovery

## Context

In distributed systems, a single business operation may span multiple services. If one step fails, we must undo previous steps through compensating transactions. The saga pattern orchestrates this: execute steps forward, and if any fails, execute compensations backward.

## Objective

Implement a saga transaction controller that executes a multi-step workflow with automatic rollback on failure.

## Specification

### Input

- `order-request` (map): Order to process
  ```clojure
  {:order-id "..." :user-id "..." :items [...] :payment {...}}
  ```

### Output

- Success: `{:status :completed :order-id "..." :steps [...]}`
- Failure: `{:status :failed :failed-step "..." :compensations-run [...] :reason "..."}`

### Rules

**Saga steps (each can fail):**

1. `reserve-inventory` - Reserves items (fails if "fail-inventory" in order-id)
2. `charge-payment` - Charges payment (fails if "fail-payment" in order-id)
3. `create-shipment` - Creates shipment (fails if "fail-shipment" in order-id)
4. `send-confirmation` - Sends email (always succeeds)

**Compensating transactions (undo steps):**
- `release-inventory` (undoes reserve-inventory)
- `refund-payment` (undoes charge-payment)
- `cancel-shipment` (undoes create-shipment)
- No compensation for send-confirmation

**On failure:**
- Run compensations for completed steps in reverse order
- Return failure with details

## Examples

```clojure
(process-order {:order-id "ORD-123" :user-id "U1" :items [...] :payment {...}})
;; => {:status :completed :order-id "ORD-123" :steps ["inventory-reserved" "payment-charged" "shipment-created" "confirmation-sent"]}

(process-order {:order-id "ORD-fail-payment" :user-id "U1" :items [...] :payment {...}})
;; => {:status :failed :failed-step "charge-payment" :compensations-run ["release-inventory"] :reason "Payment failed"}
```

## Tips

- Track completed steps as you go
- On error, run compensations in reverse
- Use try-catch or error map pattern
- Each step returns {:success boolean :data ...} or {:error "..."}

## Testing your solution

```bash
cd challenges/075-saga-transaction/
clj -M solution.clj
```
