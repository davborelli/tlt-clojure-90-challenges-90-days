# 034 - Coerce User Data Types

**Level**: 7/18
**Type**: Adapter
**Concepts**: Multiple type coercions, String to typed values, Adapter transformations

## Context

When receiving data from web forms, CSV files, or external APIs, all values typically arrive as strings. Before using this data in business logic, we need to coerce these strings into appropriate types: numbers for calculations, booleans for flags, and keywords for status codes.

## Objective

Implement an adapter function that transforms a user data map with string values into properly typed values (integers, booleans, keywords).

## Specification

### Input

- `raw-user` (map): User data with all string values
  ```clojure
  {:name "..." :age "..." :active "..." :role "..."}
  ```

### Output

- (map): User data with properly typed values
  ```clojure
  {:name string :age integer :active boolean :role keyword}
  ```

### Rules

- `:name` stays as string
- `:age` converts from string to integer
- `:active` converts from "true"/"false" string to boolean
- `:role` converts from string to keyword
- Function must be pure

## Examples

### Example 1
```clojure
(coerce-user-types {:name "John" :age "25" :active "true" :role "admin"})
;; => {:name "John" :age 25 :active true :role :admin}
```

### Example 2
```clojure
(coerce-user-types {:name "Jane" :age "30" :active "false" :role "user"})
;; => {:name "Jane" :age 30 :active false :role :user}
```

### Example 3
```clojure
(coerce-user-types {:name "Bob" :age "45" :active "true" :role "moderator"})
;; => {:name "Bob" :age 45 :active true :role :moderator}
```

## Tips

- Use `Integer/parseInt` for string to integer conversion
- Use `= "true"` to convert string to boolean
- Use `keyword` function to convert string to keyword
- Build the result map field by field using literal syntax
- Consider using `let` to destructure input for clarity

## Testing your solution

```bash
cd challenges/034-coerce-user-types/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-034.solution)
(challenge-034.solution/-test)
```
