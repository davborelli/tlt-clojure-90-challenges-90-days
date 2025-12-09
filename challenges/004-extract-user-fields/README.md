# 004 - Extract User Fields

**Level**: 1/18
**Type**: Adapter
**Concepts**: Map manipulation, select-keys, Subset extraction

## Context

When working with APIs or databases, we often receive more data than we need. Extracting only the relevant fields creates cleaner, more focused data structures and reduces memory usage.

## Objective

Implement an adapter function that extracts only specific fields from a user map, creating a subset with just the needed information.

## Specification

### Input

- `user` (map): A map with keys `:name`, `:email`, `:age`, `:address`, `:phone`

### Output

- (map): A map containing only `:name` and `:email` keys

### Rules

- Extract only `:name` and `:email` fields
- Ignore all other fields in the input
- Preserve the values unchanged
- Function must be pure

## Examples

### Example 1
```clojure
(extract-contact-info {:name "John Doe"
                       :email "john@example.com"
                       :age 30
                       :address "123 Main St"
                       :phone "555-1234"})
;; => {:name "John Doe" :email "john@example.com"}
```

### Example 2
```clojure
(extract-contact-info {:name "Jane Smith"
                       :email "jane@example.com"
                       :age 25
                       :address "456 Oak Ave"
                       :phone "555-5678"})
;; => {:name "Jane Smith" :email "jane@example.com"}
```

### Example 3
```clojure
(extract-contact-info {:name "Bob Johnson"
                       :email "bob@test.com"
                       :age 40
                       :address "789 Pine Rd"
                       :phone "555-9999"})
;; => {:name "Bob Johnson" :email "bob@test.com"}
```

## Tips

- Use `select-keys` to extract specific keys from a map
- `select-keys` takes a map and a collection of keys to keep
- The function signature is: `(select-keys map [key1 key2 ...])`
- This is more concise than manual destructuring for simple extractions

## Testing your solution

```bash
cd challenges/004-extract-user-fields/
clj -M solution.clj
```
