# 040 - Validate and Process Order

**Level**: 8/18
**Type**: Controller
**Concepts**: Multi-step validation, or composition, Error handling, Sequential operations

## Context

Order processing systems need to validate multiple aspects of an order before acceptance: customer information, product availability, payment amount, and shipping details. Each validation can fail independently, and we want to return the first error encountered or proceed to processing if all validations pass.

## Objective

Implement a controller function that validates an order through multiple checks and processes it if all validations pass, using `or` composition for error handling.

## Specification

### Input

- `order` (map): Order data with `:customer-id`, `:product-id`, `:quantity`, `:total`

### Output

- (map): Result with `:status` and either `:message` (error) or `:order-id` (success)

### Rules

**Validation checks (return error map or nil):**
1. Customer ID must be positive: else `{:status :error :message "Invalid customer ID"}`
2. Product ID must be positive: else `{:status :error :message "Invalid product ID"}`
3. Quantity must be > 0 and <= 100: else `{:status :error :message "Invalid quantity"}`
4. Total must be >= 10.0: else `{:status :error :message "Order total too low"}`

**If all validations pass:**
- Return `{:status :success :order-id "ORD-{customer-id}-{product-id}"}`

- Use `or` composition for validation flow
- Function must be pure

## Examples

### Example 1
```clojure
(process-order {:customer-id 123 :product-id 456 :quantity 5 :total 99.99})
;; => {:status :success :order-id "ORD-123-456"}
```

### Example 2
```clojure
(process-order {:customer-id -1 :product-id 456 :quantity 5 :total 99.99})
;; => {:status :error :message "Invalid customer ID"}
```

### Example 3
```clojure
(process-order {:customer-id 123 :product-id 456 :quantity 150 :total 99.99})
;; => {:status :error :message "Invalid quantity"}
```

### Example 4
```clojure
(process-order {:customer-id 123 :product-id 456 :quantity 1 :total 5.00})
;; => {:status :error :message "Order total too low"}
```

## Tips

- Create helper validation functions that return error map or nil
- Use `when` for each validation (returns nil if condition is false)
- Use `or` to chain validations: `(or (check1) (check2) ... (success))`
- Build order-id using `str`: `(str "ORD-" customer-id "-" product-id)`
- Each validation checks ONE thing and returns specific error message

## Testing your solution

```bash
cd challenges/040-validate-process-order/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-040.solution)
(challenge-040.solution/-test)
```
