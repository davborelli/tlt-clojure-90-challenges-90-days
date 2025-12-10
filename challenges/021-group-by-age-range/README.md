# 021 - Group Users by Age Range

**Level**: 5/18
**Type**: Pure Function
**Concepts**: group-by, Custom grouping logic, Data categorization

## Context

Analytics and reporting often require grouping data into categories. When analyzing user demographics, we might want to group users by age ranges (child, teen, adult, senior) rather than exact ages. This makes trends and patterns easier to identify.

## Objective

Implement a pure function that groups users into age categories: `:child` (0-12), `:teen` (13-17), `:adult` (18-64), and `:senior` (65+).

## Specification

### Input

- `users` (collection): A collection of user maps, each with `:name` and `:age` keys

### Output

- (map): A map with keys `:child`, `:teen`, `:adult`, `:senior`, where each value is a collection of users in that category

### Rules

- Age ranges:
  - `:child` - ages 0-12 (inclusive)
  - `:teen` - ages 13-17 (inclusive)
  - `:adult` - ages 18-64 (inclusive)
  - `:senior` - ages 65+ (inclusive)
- Return all four categories even if some are empty collections
- Preserve complete user maps in each group
- Function must be pure

## Examples

### Example 1
```clojure
(group-by-age [{:name "Alice" :age 10}
               {:name "Bob" :age 16}
               {:name "Charlie" :age 30}
               {:name "Diana" :age 70}])
;; => {:child [{:name "Alice" :age 10}]
;;     :teen [{:name "Bob" :age 16}]
;;     :adult [{:name "Charlie" :age 30}]
;;     :senior [{:name "Diana" :age 70}]}
```

### Example 2
```clojure
(group-by-age [{:name "John" :age 25}
               {:name "Jane" :age 30}])
;; => {:child []
;;     :teen []
;;     :adult [{:name "John" :age 25} {:name "Jane" :age 30}]
;;     :senior []}
```

### Example 3
```clojure
(group-by-age [])
;; => {:child [] :teen [] :adult [] :senior []}
```

## Tips

- Use `group-by` with a custom classifier function
- Classifier should return age category based on age value
- Use `cond` to determine which category an age falls into
- Initialize result with all four categories to ensure they're always present
- Pattern: `(group-by classifier-fn collection)`

## Testing your solution

```bash
cd challenges/021-group-by-age-range/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-021.solution)
(challenge-021.solution/-test)
```
