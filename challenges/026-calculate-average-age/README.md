# 026 - Calculate Average Age

**Level**: 6/18
**Type**: Pure Function
**Concepts**: reduce with calculation, Division, Aggregation with average

## Context

Calculating averages is a fundamental aggregation operation in data analysis. Unlike simple sums, averages require both summing values and dividing by count, which demonstrates combining multiple operations in a reduce.

## Objective

Implement a pure function that calculates the average age of users in a collection. Return 0 if the collection is empty.

## Specification

### Input

- `users` (collection): Collection of user maps with `:age` key

### Output

- (number): Average age as a decimal, or 0 if collection is empty

### Rules

- Calculate sum of all ages
- Divide by count of users
- Return 0 for empty collection (avoid division by zero)
- Return average as decimal (not rounded)
- Function must be pure

## Examples

### Example 1
```clojure
(average-age [{:name "John" :age 20}
              {:name "Jane" :age 30}
              {:name "Bob" :age 40}])
;; => 30.0
```

### Example 2
```clojure
(average-age [{:name "Alice" :age 25}
              {:name "Charlie" :age 35}])
;; => 30.0
```

### Example 3
```clojure
(average-age [])
;; => 0
```

## Tips

- Use `reduce` to sum ages
- Get count with `(count users)`
- Formula: (/ sum count)
- Check for empty collection first
- Alternative: `(/ (apply + (map :age users)) (count users))`

## Testing your solution

```bash
cd challenges/026-calculate-average-age/
clj -M solution.clj
```
