# 009 - Remove Sensitive Fields

**Level**: 2/18
**Type**: Adapter
**Concepts**: Map manipulation, dissoc, Field removal

## Context

When sending data to clients or external systems, we must remove sensitive information like passwords, social security numbers, or internal IDs. This protects user privacy and system security.

## Objective

Implement an adapter function that removes sensitive fields (`:password` and `:ssn`) from a user map.

## Specification

### Input

- `user` (map): A map with keys `:name`, `:email`, `:password`, `:ssn`, `:age`

### Output

- (map): The same map without `:password` and `:ssn` keys

### Rules

- Remove `:password` field
- Remove `:ssn` field
- Preserve all other fields
- Do not modify the original map (return new map)
- Function must be pure

## Examples

### Example 1
```clojure
(remove-sensitive {:name "John"
                   :email "john@example.com"
                   :password "secret123"
                   :ssn "123-45-6789"
                   :age 30})
;; => {:name "John" :email "john@example.com" :age 30}
```

### Example 2
```clojure
(remove-sensitive {:name "Jane"
                   :email "jane@example.com"
                   :password "pass456"
                   :ssn "987-65-4321"
                   :age 25})
;; => {:name "Jane" :email "jane@example.com" :age 25}
```

### Example 3
```clojure
(remove-sensitive {:name "Bob"
                   :email "bob@test.com"
                   :password "mypass"
                   :ssn "111-22-3333"
                   :age 40})
;; => {:name "Bob" :email "bob@test.com" :age 40}
```

## Tips

- Use `dissoc` to remove keys from a map
- `dissoc` returns a new map without modifying the original
- Syntax: `(dissoc map key1 key2 ...)`
- You can remove multiple keys at once
- If a key doesn't exist, `dissoc` just ignores it (no error)

## Testing your solution

```bash
cd challenges/009-remove-sensitive-fields/
clj -M solution.clj
```
