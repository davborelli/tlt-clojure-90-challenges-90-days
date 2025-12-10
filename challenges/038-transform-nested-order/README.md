# 038 - Transform Nested Order

**Level**: 8/18
**Type**: Adapter
**Concepts**: Nested transformations, get-in/assoc-in, Deep structure manipulation

## Context

E-commerce systems receive orders from various sources (APIs, mobile apps, web forms) in different formats. Before processing, these nested order structures need to be transformed to match the internal domain model, extracting and reorganizing deeply nested information.

## Objective

Implement an adapter function that transforms a nested order structure from external format to a flattened internal format, extracting customer and shipping information.

## Specification

### Input

- `external-order` (map): Nested order structure
  ```clojure
  {:order-id "..."
   :customer {:personal {:name "..." :email "..."}
              :shipping {:address {:street "..." :city "..." :zip "..."}}}
   :total "..."}
  ```

### Output

- (map): Flattened order structure
  ```clojure
  {:id "..."
   :customer-name "..."
   :customer-email "..."
   :shipping-street "..."
   :shipping-city "..."
   :shipping-zip "..."
   :total (parsed as number)}
  ```

### Rules

- Extract `:order-id` → `:id`
- Extract customer name from `[:customer :personal :name]` → `:customer-name`
- Extract email from `[:customer :personal :email]` → `:customer-email`
- Extract street from `[:customer :shipping :address :street]` → `:shipping-street`
- Extract city from `[:customer :shipping :address :city]` → `:shipping-city`
- Extract zip from `[:customer :shipping :address :zip]` → `:shipping-zip`
- Convert `:total` from string to double
- Function must be pure

## Examples

### Example 1
```clojure
(transform-order
  {:order-id "ORD-123"
   :customer {:personal {:name "John Doe" :email "john@example.com"}
              :shipping {:address {:street "123 Main St"
                                   :city "Springfield"
                                   :zip "12345"}}}
   :total "99.50"})
;; => {:id "ORD-123"
;;     :customer-name "John Doe"
;;     :customer-email "john@example.com"
;;     :shipping-street "123 Main St"
;;     :shipping-city "Springfield"
;;     :shipping-zip "12345"
;;     :total 99.5}
```

## Tips

- Use `get-in` to extract values from nested paths
- Path format: `[:customer :personal :name]`
- Use `Double/parseDouble` to convert string to double
- Build result map with literal syntax, extracting each field
- Consider using `let` to destructure or bind intermediate values
- All paths are 2-3 levels deep

## Testing your solution

```bash
cd challenges/038-transform-nested-order/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-038.solution)
(challenge-038.solution/-test)
```
