# 047 - Extract and Transform with Destructuring

**Level**: 10/18
**Type**: Pure Function
**Concepts**: Advanced destructuring, Data extraction, Transformation pipelines

## Context

Complex data structures often contain nested information that needs to be extracted, validated, and transformed. Advanced destructuring allows us to pull out exactly the fields we need from nested maps in a single operation, making the code more concise and declarative than using multiple `get-in` calls.

## Objective

Implement a function that uses advanced destructuring to extract nested fields from a transaction map and transform them into a summary report with calculated fields.

## Specification

### Input

- `transaction` (map): Nested transaction data
  ```clojure
  {:id "..."
   :customer {:name "..." :tier :...}
   :payment {:method :... :amount ... :currency "..."}
   :metadata {:timestamp "..." :ip "..."}}
  ```

### Output

- (map): Flattened summary with calculated fields
  ```clojure
  {:transaction-id "..."
   :customer-name "..."
   :customer-tier :...
   :payment-amount ...
   :payment-currency "..."
   :is-premium-customer boolean
   :is-high-value boolean}
  ```

### Rules

- Use destructuring in function parameters or `let` to extract nested fields
- Calculate `:is-premium-customer` → `true` if tier is `:gold` or `:platinum`
- Calculate `:is-high-value` → `true` if amount > 1000
- Extract only the specified fields (ignore `:metadata`)
- Function must be pure

## Examples

### Example 1
```clojure
(extract-transaction-summary
  {:id "TXN-001"
   :customer {:name "Alice" :tier :gold}
   :payment {:method :credit-card :amount 1500.00 :currency "USD"}
   :metadata {:timestamp "2024-01-15" :ip "192.168.1.1"}})
;; => {:transaction-id "TXN-001"
;;     :customer-name "Alice"
;;     :customer-tier :gold
;;     :payment-amount 1500.0
;;     :payment-currency "USD"
;;     :is-premium-customer true
;;     :is-high-value true}
```

### Example 2
```clojure
(extract-transaction-summary
  {:id "TXN-002"
   :customer {:name "Bob" :tier :silver}
   :payment {:method :debit-card :amount 500.00 :currency "USD"}
   :metadata {:timestamp "2024-01-16" :ip "192.168.1.2"}})
;; => {:transaction-id "TXN-002"
;;     :customer-name "Bob"
;;     :customer-tier :silver
;;     :payment-amount 500.0
;;     :payment-currency "USD"
;;     :is-premium-customer false
;;     :is-high-value false}
```

## Tips

- Use nested destructuring: `{:keys [id customer payment]}`
- Then destructure nested maps: `{:keys [name tier]} customer`
- Or use nested destructuring in one step: `{customer {:keys [name tier]}}`
- Calculate boolean fields with `contains?` or direct comparison
- Pattern: `(#{:gold :platinum} tier)` returns tier if in set, nil otherwise (truthy check)
- Destructuring makes extraction declarative and readable

## Testing your solution

```bash
cd challenges/047-extract-and-transform/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-047.solution)
(challenge-047.solution/-test)
```
