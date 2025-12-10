# 024 - Flatten Nested User Data

**Level**: 5/18
**Type**: Adapter
**Concepts**: Nested map transformation, Flattening structures, assoc from nested values

## Context

APIs often return nested data, but sometimes we need flat structures for databases, CSV exports, or simple displays. Flattening transforms `{:user {:name "..." :age ...}}` into `{:name "..." :age ...}`.

## Objective

Implement an adapter function that flattens a nested user profile into a single-level map.

## Specification

### Input

- `profile` (map): Nested map with structure:
  ```clojure
  {:user {:name "..." :age ... :email "..."}}
  ```

### Output

- (map): Flat map with `:name`, `:age`, and `:email` at top level

### Rules

- Extract `:name`, `:age`, `:email` from `[:user ...]` path
- Return flat map at single level
- Preserve all extracted values
- Function must be pure

## Examples

### Example 1
```clojure
(flatten-profile {:user {:name "John" :age 25 :email "john@example.com"}})
;; => {:name "John" :age 25 :email "john@example.com"}
```

### Example 2
```clojure
(flatten-profile {:user {:name "Jane" :age 30 :email "jane@test.com"}})
;; => {:name "Jane" :age 30 :email "jane@test.com"}
```

## Tips

- Access nested map with `(:user profile)`
- Or use `get-in` for safety
- Simply return the inner `:user` map (it's already flat)

## Testing your solution

```bash
cd challenges/024-flatten-user-data/
clj -M solution.clj
```
