# 060 - Order Fulfillment

**Level**: 12/18
**Type**: Controller
**Concepts**: Complex pipeline, Multiple validations, State enrichment, Threading macros

## Context

Order fulfillment involves multiple sequential operations: validate order, check inventory, calculate shipping, reserve items, generate invoice, and update order status. Each step validates conditions and enriches the order state with additional information. A comprehensive pipeline orchestrates all steps.

## Objective

Implement an order fulfillment controller that uses threading macros to orchestrate validation, inventory checks, calculations, and state updates through a multi-step pipeline.

## Specification

### Input

- `order-request` (map): Initial order
  ```clojure
  {:order-id "..."
   :customer-id "..."
   :items [{:product-id "..." :quantity ...} ...]
   :shipping-address {:...}}
  ```

### Output

- (map): Fulfilled order state or error
  ```clojure
  {:order-id "..."
   :customer-id "..."
   :items [...]
   :shipping-address {...}
   :inventory-reserved boolean
   :shipping-cost ...
   :subtotal ...
   :total ...
   :invoice-id "..."
   :status :fulfilled}
  ```

Or error: `{:status :error :message "..."}`

### Rules

**Helper functions to implement:**

1. `validate-order` - Validates order has required fields
   - Must have :order-id, :customer-id, :items (non-empty), :shipping-address
   - Returns state with `:validated true` or error map

2. `check-inventory` - Simulates inventory check
   - For simplicity, always succeeds
   - Adds `:inventory-reserved true`

3. `calculate-subtotal` - Sums item prices
   - For each item: `{:product-id ... :quantity ... :unit-price ...}`
   - Subtotal = sum of (quantity * unit-price) for all items
   - Adds `:subtotal` to state

4. `calculate-shipping` - Calculates shipping cost
   - Shipping = $10 if subtotal < 100, free otherwise
   - Adds `:shipping-cost` to state

5. `calculate-total` - Calculates final total
   - Total = subtotal + shipping-cost
   - Adds `:total` to state

6. `generate-invoice` - Generates invoice ID
   - Format: `"INV-" + order-id`
   - Adds `:invoice-id` to state

7. `mark-fulfilled` - Marks order as fulfilled
   - Sets `:status :fulfilled`

**Main function:**
- `fulfill-order` - Uses `->` to orchestrate all steps
- Short-circuit on validation error
- Pattern: `(-> order validate check-inv calc-subtotal calc-shipping calc-total gen-invoice mark-fulfilled)`

## Examples

### Example 1
```clojure
(fulfill-order
  {:order-id "ORD-001"
   :customer-id "CUST-123"
   :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
           {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
   :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}})
;; => {:order-id "ORD-001"
;;     :customer-id "CUST-123"
;;     :items [...]
;;     :shipping-address {...}
;;     :validated true
;;     :inventory-reserved true
;;     :subtotal 110.0
;;     :shipping-cost 0.0
;;     :total 110.0
;;     :invoice-id "INV-ORD-001"
;;     :status :fulfilled}
```

### Example 2 (validation failure)
```clojure
(fulfill-order
  {:order-id "ORD-002"
   :customer-id "CUST-456"
   :items []  ; Empty items
   :shipping-address {...}})
;; => {:status :error :message "Order must have items"}
```

## Tips

- Validate first, return error map immediately if validation fails
- Use `->` to thread state through all steps
- Each helper takes state, returns transformed state
- Calculate subtotal: `(reduce + (map #(* (:quantity %) (:unit-price %)) items))`
- Shipping logic: `(if (< subtotal 100) 10.0 0.0)`
- Each step adds fields to accumulating state map

## Testing your solution

```bash
cd challenges/060-order-fulfillment/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-060.solution)
(challenge-060.solution/-test)
```
