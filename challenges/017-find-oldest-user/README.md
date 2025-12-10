# 017 - Find Oldest User

**Level**: 4/18
**Type**: Pure Function
**Concepts**: reduce, max-key, aggregation patterns

## Context

Finding the maximum or minimum value in a collection is a common aggregation task. When working with complex data structures like user maps, we need to find the maximum based on a specific field (like age) while returning the entire object, not just the max value.

## Objective

Implement a pure function that finds the oldest user (highest age) from a collection of user maps. Return the entire user map of the oldest person.

## Specification

### Input

- `users` (collection): A non-empty collection of user maps, each with `:name` and `:age` keys

### Output

- (map): The user map with the highest age value

### Rules

- Find the user with the maximum age
- Return the complete user map, not just the age
- Assume the collection is non-empty
- If multiple users have the same maximum age, return any one of them
- Function must be pure

## Examples

### Example 1
```clojure
(find-oldest [{:name "John" :age 25}
              {:name "Jane" :age 30}
              {:name "Bob" :age 20}])
;; => {:name "Jane" :age 30}
```

### Example 2
```clojure
(find-oldest [{:name "Alice" :age 45}
              {:name "Charlie" :age 50}])
;; => {:name "Charlie" :age 50}
```

### Example 3
```clojure
(find-oldest [{:name "Solo" :age 99}])
;; => {:name "Solo" :age 99}
```

### Example 4
```clojure
(find-oldest [{:name "Eve" :age 35}
              {:name "Frank" :age 35}
              {:name "Grace" :age 30}])
;; => {:name "Eve" :age 35}  ; or Frank (both have max age)
```

## Tips

- Use `reduce` with a comparison function
- Compare users by their `:age` field
- Use `max-key` function for cleaner solution
- The accumulator should track the current oldest user
- Consider: `(apply max-key :age users)` as an elegant solution

## Testing your solution

```bash
cd challenges/017-find-oldest-user/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-017.solution)
(challenge-017.solution/-test)
```
