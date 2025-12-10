# 027 - Check if All Users are Adults

**Level**: 6/18
**Type**: Pure Function
**Concepts**: every?, Predicate composition, Universal quantification

## Context

Sometimes we need to verify that all items in a collection meet a condition, not just some. The `every?` function provides this universal quantification check.

## Objective

Implement a pure function that checks if all users in a collection are adults (age >= 18).

## Specification

### Input

- `users` (collection): Collection of user maps with `:age` key

### Output

- (boolean): `true` if all users are 18+, `false` otherwise

### Rules

- Adult is defined as age >= 18
- Return `true` for empty collection (vacuous truth)
- Check all users meet the condition
- Function must be pure

## Examples

### Example 1
```clojure
(all-adults? [{:name "John" :age 25}
              {:name "Jane" :age 30}])
;; => true
```

### Example 2
```clojure
(all-adults? [{:name "John" :age 25}
              {:name "Bob" :age 17}])
;; => false
```

### Example 3
```clojure
(all-adults? [])
;; => true
```

## Tips

- Use `every?` function with a predicate
- Predicate checks if age >= 18
- `every?` returns true for empty collection
- Pattern: `(every? predicate coll)`

## Testing your solution

```bash
cd challenges/027-all-adults/
clj -M solution.clj
```
