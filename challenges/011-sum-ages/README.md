# 011 - Sum Ages

**Level**: 3/18
**Type**: Pure Function
**Concepts**: Collection reduction, reduce function, Aggregation

## Context

Aggregating data from collections is a fundamental operation. Whether calculating totals, averages, or other aggregate values, the reduce function is the tool of choice in functional programming.

## Objective

Implement a pure function that calculates the total sum of ages from a list of user maps.

## Specification

### Input

- `users` (vector): A vector of maps, each with `:name` and `:age` keys

### Output

- (integer): The sum of all ages

### Rules

- Sum all `:age` values from the user maps
- Return 0 for empty vector
- Function must be pure

## Examples

### Example 1
```clojure
(sum-ages [{:name "John" :age 25}
           {:name "Jane" :age 30}
           {:name "Bob" :age 45}])
;; => 100
```

### Example 2
```clojure
(sum-ages [{:name "Alice" :age 18}])
;; => 18
```

### Example 3
```clojure
(sum-ages [])
;; => 0
```

### Example 4
```clojure
(sum-ages [{:name "Diana" :age 20}
           {:name "Eve" :age 22}
           {:name "Frank" :age 28}])
;; => 70
```

## Tips

- Use `reduce` to accumulate the sum
- `reduce` takes a function, optional initial value, and a collection
- The reducing function takes two arguments: accumulator and current element
- Extract `:age` from each user map using `(:age user)`
- Initial value of 0 handles the empty collection case
- Alternatively, combine `map` and `reduce`: first extract ages, then sum

## Testing your solution

```bash
cd challenges/011-sum-ages/
clj -M solution.clj
```
