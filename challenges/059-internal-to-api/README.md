# 059 - Internal to API (Part 2 of Bidirectional Pair)

**Level**: 12/18
**Type**: Adapter
**Concepts**: Bidirectional transformation, Internal → API, Building nested structures

## Context

This is Part 2 of the bidirectional transformation pair (Challenge 058 implemented API → Internal). When sending data to external APIs, we need to transform our clean internal models into the format the API expects: camelCase keys, nested structures, string enums, and added metadata.

## Objective

Implement an adapter that transforms an internal product model into the nested API request format expected by the external system.

## Specification

### Input

- `internal-product` (map): Internal domain model
  ```clojure
  {:product-id "..."
   :name "..."
   :category :...
   :price ...
   :currency :...
   :stock ...
   :warehouse-id "..."
   :warehouse-location "..."}
  ```

### Output

- (map): External API format
  ```clojure
  {:productId "..."
   :productDetails {:productName "..."
                    :category "..."
                    :pricing {:basePrice ... :currency "..."}}
   :inventory {:stockLevel ...
               :warehouse {:id "..." :location "..."}}}
  ```

### Rules

- Transform and nest fields:
  - `:product-id` → `:productId`
  - `:name` → `[:productDetails :productName]`
  - `:category` (keyword) → `[:productDetails :category]` (uppercase string)
  - `:price` → `[:productDetails :pricing :basePrice]`
  - `:currency` (keyword) → `[:productDetails :pricing :currency]` (uppercase string)
  - `:stock` → `[:inventory :stockLevel]`
  - `:warehouse-id` → `[:inventory :warehouse :id]`
  - `:warehouse-location` → `[:inventory :warehouse :location]`
- Convert keyword enums to uppercase strings: `:electronics` → `"electronics"`, `:usd` → `"USD"`
- Build nested structure (reverse of flattening)
- Do NOT include :metadata (API will add it)
- Function must be pure

## Examples

### Example 1
```clojure
(internal->api
  {:product-id "PROD-123"
   :name "Laptop"
   :category :electronics
   :price 999.99
   :currency :usd
   :stock 50
   :warehouse-id "WH-001"
   :warehouse-location "New York"})
;; => {:productId "PROD-123"
;;     :productDetails {:productName "Laptop"
;;                      :category "electronics"
;;                      :pricing {:basePrice 999.99 :currency "USD"}}
;;     :inventory {:stockLevel 50
;;                 :warehouse {:id "WH-001" :location "New York"}}}
```

## Tips

- Build nested maps directly: `{:productDetails {:productName name ...}}`
- Convert keyword to uppercase string: `(str/upper-case (name :usd))` → `"USD"`
- For category, keep lowercase: `(name :electronics)` → `"electronics"`
- Destructure input, build nested output
- This is the exact inverse of Challenge 058
- Round-trip test: `(internal->api (api->internal response))` should match `response` (minus metadata)

## Testing your solution

```bash
cd challenges/059-internal-to-api/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-059.solution)
(challenge-059.solution/-test)
```

## Related Challenge

See Challenge 058 for the inverse transformation (api→internal).
