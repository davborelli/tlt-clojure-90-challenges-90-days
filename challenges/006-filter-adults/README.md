# 006 - Filter Adults

**Level**: 2/18
**Type**: Pure Function
**Concepts**: Collection filtering, filter function, Higher-order functions

## Context

When processing lists of users, we often need to filter them based on criteria. For example, filtering users who are of legal age is common in age-restricted services or when determining eligibility for certain features.

## Objective

Implement a pure function that filters a list of user maps, keeping only those who are adults (age >= 18).

## Specification

### Input

- `users` (vector): A vector of maps, each with `:name` and `:age` keys

### Output

- (vector): A vector containing only the user maps where `:age` >= 18

### Rules

- Keep users with age >= 18
- Remove users with age < 18
- Preserve the original order
- Return empty vector if no adults found
- Function must be pure

## Examples

### Example 1
```clojure
(filter-adults [{:name "John" :age 25}
                {:name "Jane" :age 17}
                {:name "Bob" :age 30}])
;; => [{:name "John" :age 25} {:name "Bob" :age 30}]
```

### Example 2
```clojure
(filter-adults [{:name "Alice" :age 16}
                {:name "Charlie" :age 15}])
;; => []
```

### Example 3
```clojure
(filter-adults [{:name "Diana" :age 18}
                {:name "Eve" :age 18}])
;; => [{:name "Diana" :age 18} {:name "Eve" :age 18}]
```

### Example 4
```clojure
(filter-adults [])
;; => []
```

## Tips

- Use the `filter` function to filter the collection
- `filter` takes a predicate function and a collection
- Create an anonymous function with `#()` or `fn` to check if `:age` >= 18
- You can use `(>= (:age %) 18)` inside the predicate
- `filter` returns a lazy sequence, but it will be realized when returned

## Testing your solution

```bash
cd challenges/006-filter-adults/
clj -M solution.clj
```
