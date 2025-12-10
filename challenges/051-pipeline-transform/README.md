# 051 - Pipeline Transform

**Level**: 11/18
**Type**: Pure Function
**Concepts**: comp function, Function composition, Transformation pipelines

## Context

Data transformation often involves multiple sequential operations: clean input, validate format, normalize values, calculate derived fields. Instead of nesting function calls `(f4 (f3 (f2 (f1 data))))`, we can use `comp` to create a composed function that reads more naturally and is easier to test and modify.

## Objective

Implement transformation functions that use `comp` to create a pipeline that processes user registration data through multiple steps: trim whitespace, normalize email, validate format, and add timestamp.

## Specification

### Input

- `raw-data` (map): User registration data that may have untrimmed strings
  ```clojure
  {:name "  Alice Johnson  "
   :email "  ALICE@EXAMPLE.COM  "
   :age "25"}
  ```

### Output

- (map): Cleaned and enriched registration data
  ```clojure
  {:name "Alice Johnson"
   :email "alice@example.com"
   :age 25
   :registered-at "2024-01-15T10:00:00"}
  ```

### Rules

**Helper functions to implement:**

1. `trim-strings` - Trims whitespace from :name and :email
2. `normalize-email` - Converts :email to lowercase
3. `parse-age` - Converts :age string to integer
4. `add-timestamp` - Adds :registered-at field with current timestamp (simulated as "2024-01-15T10:00:00")

**Main function:**
- `process-registration` - Uses `comp` to create pipeline of transformations
- Pattern: `(def pipeline (comp add-timestamp parse-age normalize-email trim-strings))`
- Apply pipeline to data: `(pipeline raw-data)`
- Or use threading: `(-> data trim-strings normalize-email parse-age add-timestamp)`

### Function Order (important!)

- `comp` composes right-to-left: `(comp f g h)` means `(f (g (h x)))`
- So list functions in reverse order: `(comp add-timestamp parse-age normalize-email trim-strings)`
- Or use `->` threading which reads left-to-right (more intuitive)

## Examples

### Example 1
```clojure
(process-registration
  {:name "  Alice Johnson  "
   :email "  ALICE@EXAMPLE.COM  "
   :age "25"})
;; => {:name "Alice Johnson"
;;     :email "alice@example.com"
;;     :age 25
;;     :registered-at "2024-01-15T10:00:00"}
```

### Example 2
```clojure
(process-registration
  {:name "Bob Smith"
   :email "BOB@EXAMPLE.COM"
   :age "30"})
;; => {:name "Bob Smith"
;;     :email "bob@example.com"
;;     :age 30
;;     :registered-at "2024-01-15T10:00:00"}
```

## Tips

- Use `update` to transform specific keys: `(update data :email str/lower-case)`
- Use `->` for readability: `(-> data trim normalize parse enrich)`
- `comp` creates a new function: `(def pipeline (comp f g h))`
- Each helper takes full map, returns transformed map
- Pattern: `(defn step [data] (update data :field transform-fn))`

## Testing your solution

```bash
cd challenges/051-pipeline-transform/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-051.solution)
(challenge-051.solution/-test)
```
