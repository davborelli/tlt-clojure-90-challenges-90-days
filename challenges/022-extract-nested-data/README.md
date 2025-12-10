# 022 - Extract Nested Data

**Level**: 5/18
**Type**: Pure Function
**Concepts**: Nested destructuring, get-in, Extracting from deep structures

## Context

Real-world data often arrives in deeply nested structures (JSON from APIs, configuration files, database results). We need to extract specific values from these nested structures safely and concisely.

## Objective

Implement a pure function that extracts city and zip code from a nested user profile structure.

## Specification

### Input

- `profile` (map): A nested map with structure:
  ```clojure
  {:user {:name "..."
          :contact {:address {:city "..."
                              :zip "..."}}}}
  ```

### Output

- (map): Flat map with `:city` and `:zip` keys

### Rules

- Extract `:city` from `[:user :contact :address :city]` path
- Extract `:zip` from `[:user :contact :address :zip]` path
- Return flat map `{:city "..." :zip "..."}`
- Function must be pure

## Examples

### Example 1
```clojure
(extract-location {:user {:name "John"
                          :contact {:address {:city "NYC"
                                              :zip "10001"}}}})
;; => {:city "NYC" :zip "10001"}
```

### Example 2
```clojure
(extract-location {:user {:name "Jane"
                          :contact {:address {:city "LA"
                                              :zip "90001"}}}})
;; => {:city "LA" :zip "90001"}
```

## Tips

- Use `get-in` to safely extract nested values
- Path: `[:user :contact :address :city]`
- Build result map with `assoc` or map literal
- Alternative: nested destructuring with `:keys`

## Testing your solution

```bash
cd challenges/022-extract-nested-data/
clj -M solution.clj
```
