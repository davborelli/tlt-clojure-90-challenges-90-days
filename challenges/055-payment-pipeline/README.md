# 055 - Payment Pipeline

**Level**: 11/18
**Type**: Controller
**Concepts**: Threading macros, Multi-step pipelines, State transformation

## Context

Payment processing involves multiple sequential steps: validate payment data, check user balance, apply fees, process transaction, update balance, and log the operation. Each step transforms the payment state, and the final state contains the complete transaction record. Threading macros make this pipeline readable and maintainable.

## Objective

Implement a payment processing controller that uses the `->` threading macro to transform payment state through multiple steps.

## Specification

### Input

- `payment-request` (map): Initial payment request
  ```clojure
  {:user-id "..."
   :amount ...
   :recipient "..."}
  ```

### Output

- (map): Final transaction state with all processing steps applied
  ```clojure
  {:user-id "..."
   :amount ...
   :recipient "..."
   :fee ...
   :total ...
   :transaction-id "..."
   :timestamp "..."
   :status :completed}
  ```

### Rules

**Helper functions to implement:**

1. `validate-payment` - Validates payment request
   - Returns state with `:status :validated` if amount > 0
   - Returns error state `{:status :error :message "Invalid amount"}` if amount <= 0

2. `calculate-fee` - Calculates transaction fee
   - Fee is 2% of amount: `(* amount 0.02)`
   - Adds `:fee` to state

3. `calculate-total` - Calculates total charge
   - Total = amount + fee
   - Adds `:total` to state

4. `generate-transaction-id` - Generates unique transaction ID
   - Format: `"TXN-" + user-id + "-" + (hash amount)`
   - Adds `:transaction-id` to state

5. `add-timestamp` - Adds processing timestamp
   - Simulated: `"2024-01-15T10:30:00"`
   - Adds `:timestamp` to state

6. `mark-completed` - Marks transaction as completed
   - Sets `:status :completed`

**Main function:**
- `process-payment` - Uses `->` to thread state through all steps
- Pattern: `(-> payment-request validate calculate-fee calculate-total generate-id add-timestamp mark-completed)`
- Return final state or error state if validation fails

## Examples

### Example 1
```clojure
(process-payment
  {:user-id "USER-123"
   :amount 100.00
   :recipient "MERCHANT-456"})
;; => {:user-id "USER-123"
;;     :amount 100.0
;;     :recipient "MERCHANT-456"
;;     :fee 2.0
;;     :total 102.0
;;     :transaction-id "TXN-USER-123-..."
;;     :timestamp "2024-01-15T10:30:00"
;;     :status :completed}
```

### Example 2
```clojure
(process-payment
  {:user-id "USER-456"
   :amount 0
   :recipient "MERCHANT-789"})
;; => {:status :error :message "Invalid amount"}
```

## Tips

- Use `->` to thread state through transformations
- Each helper takes state, returns transformed state
- Use `if` in validate to return error or continue
- Short-circuit pattern: if validation fails, return error immediately
- Calculate fee: `(assoc state :fee (* (:amount state) 0.02))`
- Use `str` to build transaction ID: `(str "TXN-" user-id "-" (hash amount))`

## Testing your solution

```bash
cd challenges/055-payment-pipeline/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-055.solution)
(challenge-055.solution/-test)
```
