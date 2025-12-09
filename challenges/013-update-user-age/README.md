# 013 - Update User Age

**Level**: 3/18
**Type**: Adapter
**Concepts**: Map manipulation, update function, Value transformation

## Context

Updating values in maps based on their current value is a common pattern. Rather than replacing a value directly, we often want to transform it - incrementing a counter, applying a calculation, or modifying based on business logic.

## Objective

Implement an adapter function that increments a user's age by 1 (simulating a birthday).

## Specification

### Input

- `user` (map): A map with `:name` and `:age` keys

### Output

- (map): The same map with `:age` incremented by 1

### Rules

- Increment the `:age` value by 1
- Preserve all other fields unchanged
- Do not modify the original map (return new map)
- Function must be pure

## Examples

### Example 1
```clojure
(birthday {:name "John" :age 25})
;; => {:name "John" :age 26}
```

### Example 2
```clojure
(birthday {:name "Jane" :age 17})
;; => {:name "Jane" :age 18}
```

### Example 3
```clojure
(birthday {:name "Bob" :age 0})
;; => {:name "Bob" :age 1}
```

### Example 4
```clojure
(birthday {:name "Alice" :age 99})
;; => {:name "Alice" :age 100}
```

## Tips

- Use `update` to transform a value based on its current value
- `update` takes a map, a key, and a function to apply to that key's value
- Syntax: `(update map key f)` or `(update map key f arg1 arg2 ...)`
- For this challenge: `(update user :age inc)`
- `inc` is a built-in function that adds 1 to a number
- `update` returns a new map without modifying the original

## Testing your solution

```bash
cd challenges/013-update-user-age/
clj -M solution.clj
```
