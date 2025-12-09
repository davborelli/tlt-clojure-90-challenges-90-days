# 014 - Add Full Name

**Level**: 3/18
**Type**: Adapter
**Concepts**: Map manipulation, Computed fields, String concatenation

## Context

Derived or computed fields are values calculated from other fields in the same record. Rather than storing redundant data, we compute it when needed. This is common for display names, totals, ages calculated from birthdates, etc.

## Objective

Implement an adapter function that adds a `:full-name` field computed from `:first-name` and `:last-name`.

## Specification

### Input

- `user` (map): A map with `:first-name` and `:last-name` keys

### Output

- (map): The same map with additional `:full-name` key containing "FirstName LastName"

### Rules

- Compute `:full-name` by concatenating `:first-name`, a space, and `:last-name`
- Preserve all existing fields
- Do not modify the original map (return new map)
- Function must be pure

## Examples

### Example 1
```clojure
(add-full-name {:first-name "John" :last-name "Doe"})
;; => {:first-name "John" :last-name "Doe" :full-name "John Doe"}
```

### Example 2
```clojure
(add-full-name {:first-name "Jane" :last-name "Smith"})
;; => {:first-name "Jane" :last-name "Smith" :full-name "Jane Smith"}
```

### Example 3
```clojure
(add-full-name {:first-name "Bob" :last-name "Johnson"})
;; => {:first-name "Bob" :last-name "Johnson" :full-name "Bob Johnson"}
```

## Tips

- Use `assoc` to add the new `:full-name` field
- Use `str` to concatenate the first name, space, and last name
- Extract values using `(:first-name user)` and `(:last-name user)`
- Formula: `(str first-name " " last-name)`
- You can destructure the map for cleaner code

## Testing your solution

```bash
cd challenges/014-add-full-name/
clj -M solution.clj
```
