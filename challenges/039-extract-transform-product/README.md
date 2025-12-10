# 039 - Extract and Transform Product

**Level**: 8/18
**Type**: Adapter
**Concepts**: Nested extraction, Collection mapping, Type coercion, Computed fields

## Context

Product catalogs from suppliers often come with nested pricing and inventory data. Before storing products in our system, we need to extract relevant fields, calculate derived values (like discounted prices), and flatten the structure for easier querying.

## Objective

Implement an adapter function that transforms a nested product structure, extracting pricing/inventory data and calculating a discounted price.

## Specification

### Input

- `external-product` (map): Nested product from supplier
  ```clojure
  {:product-id "..."
   :details {:name "..." :category "..."}
   :pricing {:base-price "..." :discount-percent "..."}
   :inventory {:stock "..." :warehouse "..."}}
  ```

### Output

- (map): Flattened product with computed fields
  ```clojure
  {:id "..."
   :name "..."
   :category "..."
   :price (double)
   :discounted-price (double, calculated)
   :stock (integer)
   :warehouse "..."}
  ```

### Rules

- Extract `:product-id` → `:id`
- Extract `[:details :name]` → `:name`
- Extract `[:details :category]` → `:category`
- Extract `[:pricing :base-price]` (string) → `:price` (double)
- Calculate `:discounted-price` = price × (1 - discount/100)
- Extract `[:inventory :stock]` (string) → `:stock` (integer)
- Extract `[:inventory :warehouse]` → `:warehouse`
- Function must be pure

## Examples

### Example 1
```clojure
(transform-product
  {:product-id "PROD-001"
   :details {:name "Laptop" :category "Electronics"}
   :pricing {:base-price "999.99" :discount-percent "10"}
   :inventory {:stock "50" :warehouse "NYC"}})
;; => {:id "PROD-001"
;;     :name "Laptop"
;;     :category "Electronics"
;;     :price 999.99
;;     :discounted-price 899.99
;;     :stock 50
;;     :warehouse "NYC"}
```

### Example 2
```clojure
(transform-product
  {:product-id "PROD-002"
   :details {:name "Mouse" :category "Accessories"}
   :pricing {:base-price "29.99" :discount-percent "20"}
   :inventory {:stock "200" :warehouse "LA"}})
;; => {:id "PROD-002"
;;     :name "Mouse"
;;     :category "Accessories"
;;     :price 29.99
;;     :discounted-price 23.99
;;     :stock 200
;;     :warehouse "LA"}
```

## Tips

- Use `get-in` for nested field extraction
- Parse price: `(Double/parseDouble price-str)`
- Parse discount: `(Double/parseDouble discount-str)`
- Calculate discount: `price * (1 - discount / 100)`
- Parse stock: `(Integer/parseInt stock-str)`
- Use `let` to bind intermediate values for clarity

## Testing your solution

```bash
cd challenges/039-extract-transform-product/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-039.solution)
(challenge-039.solution/-test)
```
