# 043 - Restructure API Response

**Level**: 9/18
**Type**: Adapter
**Concepts**: Complex nested transformation, Collection mapping, Selective extraction

## Context

External APIs often return complex nested responses with metadata, pagination, and deep hierarchies. Before using this data in our application, we need to restructure it: extract relevant fields, flatten hierarchies, transform collections, and discard unnecessary metadata.

## Objective

Implement an adapter function that transforms a complex API response with nested user data and metadata into a clean, flattened structure suitable for internal use.

## Specification

### Input

- `api-response` (map): Complex API response
  ```clojure
  {:status "success"
   :metadata {:timestamp "..." :page 1 :total 2}
   :data {:users [{:id ... :profile {:name "..." :contact {:email "..." :phone "..."}}}
                  {...}]}}
  ```

### Output

- (vector): Vector of flattened user maps
  ```clojure
  [{:user-id ... :name "..." :email "..." :phone "..."}
   {...}]
  ```

### Rules

- Extract the users collection from `[:data :users]`
- For each user, create a flat map with:
  - `:id` → `:user-id`
  - `[:profile :name]` → `:name`
  - `[:profile :contact :email]` → `:email`
  - `[:profile :contact :phone]` → `:phone`
- Discard `:status` and `:metadata`
- Return vector of transformed users
- Function must be pure

## Examples

### Example 1
```clojure
(restructure-response
  {:status "success"
   :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 2}
   :data {:users [{:id 1
                   :profile {:name "John Doe"
                             :contact {:email "john@example.com"
                                       :phone "555-0100"}}}
                  {:id 2
                   :profile {:name "Jane Smith"
                             :contact {:email "jane@example.com"
                                       :phone "555-0200"}}}]}})
;; => [{:user-id 1 :name "John Doe" :email "john@example.com" :phone "555-0100"}
;;     {:user-id 2 :name "Jane Smith" :email "jane@example.com" :phone "555-0200"}]
```

## Tips

- Extract users with `get-in`: `(get-in api-response [:data :users])`
- Use `map` to transform each user in the collection
- Use `get-in` to extract nested fields from each user
- Build the flat map for each user with literal syntax
- Consider extracting transformation logic into a helper function
- Result should be a vector (map returns a seq, use `vec` if needed)

## Testing your solution

```bash
cd challenges/043-restructure-api-response/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-043.solution)
(challenge-043.solution/-test)
```
