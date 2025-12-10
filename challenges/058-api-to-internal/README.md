# 058 - API to Internal (Part 1 of Bidirectional Pair)

**Level**: 12/18
**Type**: Adapter
**Concepts**: Bidirectional transformation, API → Internal, Complex nested structures

## Context

External APIs often send data in formats different from internal application models. API responses might use camelCase, include metadata we don't need, nest data differently, and use string enums. Adapters transform this external format into clean internal domain models.

This is Part 1 of a bidirectional pair. Challenge 059 implements the reverse (internal → API).

## Objective

Implement an adapter that transforms a complex nested API response into an internal product model, handling key transformations, nested extraction, and enum conversion.

## Specification

### Input

- `api-response` (map): External API format
  ```clojure
  {:productId "..."
   :productDetails {:productName "..."
                    :category "..."
                    :pricing {:basePrice ... :currency "..."}}
   :inventory {:stockLevel ... :warehouse {:id "..." :location "..."}}
   :metadata {:timestamp "..." :version ...}}
  ```

### Output

- (map): Internal domain model
  ```clojure
  {:product-id "..."
   :name "..."
   :category :...  ; keyword enum
   :price ...
   :currency :...  ; keyword enum
   :stock ...
   :warehouse-id "..."
   :warehouse-location "..."}
  ```

### Rules

- Extract and rename fields:
  - `:productId` → `:product-id`
  - `[:productDetails :productName]` → `:name`
  - `[:productDetails :category]` → `:category` (convert to keyword)
  - `[:productDetails :pricing :basePrice]` → `:price`
  - `[:productDetails :pricing :currency]` → `:currency` (convert to keyword)
  - `[:inventory :stockLevel]` → `:stock`
  - `[:inventory :warehouse :id]` → `:warehouse-id`
  - `[:inventory :warehouse :location]` → `:warehouse-location`
- Convert string enums to keywords: `"electronics"` → `:electronics`, `"USD"` → `:usd`
- Discard `:metadata` (not needed internally)
- Use get-in for nested extraction
- Function must be pure

## Examples

### Example 1
```clojure
(api->internal
  {:productId "PROD-123"
   :productDetails {:productName "Laptop"
                    :category "electronics"
                    :pricing {:basePrice 999.99 :currency "USD"}}
   :inventory {:stockLevel 50
               :warehouse {:id "WH-001" :location "New York"}}
   :metadata {:timestamp "2024-01-15" :version 2}})
;; => {:product-id "PROD-123"
;;     :name "Laptop"
;;     :category :electronics
;;     :price 999.99
;;     :currency :usd
;;     :stock 50
;;     :warehouse-id "WH-001"
;;     :warehouse-location "New York"}
```

## Tips

- Use get-in for nested paths: `(get-in data [:productDetails :productName])`
- Convert string to lowercase keyword: `(keyword (str/lower-case "USD"))` → `:usd`
- Destructure top-level, then extract nested with get-in
- Build result map with renamed keys
- This transformation will be reversed in Challenge 059

## Testing your solution

```bash
cd challenges/058-api-to-internal/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-058.solution)
(challenge-058.solution/-test)
```

## Related Challenge

See Challenge 059 for the reverse transformation (internal→api).
