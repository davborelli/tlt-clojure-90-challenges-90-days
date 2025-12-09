# 008 - Add Status Field

**Level**: 2/18
**Type**: Adapter
**Concepts**: Map manipulation, assoc, Adding fields

## Context

When enriching data, we often need to add new fields to existing maps. This is common when adding computed values, metadata, or default values to records before saving them or sending them to other systems.

## Objective

Implement an adapter function that adds a `:status` field to a user map with the value `:active`.

## Specification

### Input

- `user` (map): A map with `:name` and `:email` keys

### Output

- (map): The same map with an additional `:status` key set to `:active`

### Rules

- Add `:status :active` to the map
- Preserve all existing fields
- Do not modify the original map (return new map)
- Function must be pure

## Examples

### Example 1
```clojure
(add-status {:name "John" :email "john@example.com"})
;; => {:name "John" :email "john@example.com" :status :active}
```

### Example 2
```clojure
(add-status {:name "Jane" :email "jane@example.com"})
;; => {:name "Jane" :email "jane@example.com" :status :active}
```

### Example 3
```clojure
(add-status {:name "Bob" :email "bob@test.com"})
;; => {:name "Bob" :email "bob@test.com" :status :active}
```

## Tips

- Use `assoc` to add a new key-value pair to a map
- `assoc` returns a new map without modifying the original
- Syntax: `(assoc map key value)`
- You can add multiple keys at once: `(assoc map k1 v1 k2 v2)`
- Keywords are the idiomatic way to represent status values in Clojure

## Testing your solution

```bash
cd challenges/008-add-status-field/
clj -M solution.clj
```
