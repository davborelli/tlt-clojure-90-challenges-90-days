# 012 - Extract Domain

**Level**: 3/18
**Type**: Pure Function
**Concepts**: String manipulation, split function, Parsing

## Context

Parsing and extracting information from strings is a common task. Email addresses contain structured information - the domain part (after the @) often tells us which organization or service the user belongs to.

## Objective

Implement a pure function that extracts the domain part from an email address.

## Specification

### Input

- `email` (string): An email address in format "user@domain.com"

### Output

- (string): The domain part (everything after @)

### Rules

- Extract everything after the @ symbol
- Assume email contains exactly one @ symbol
- Return empty string if no @ found
- Function must be pure

## Examples

### Example 1
```clojure
(extract-domain "john@example.com")
;; => "example.com"
```

### Example 2
```clojure
(extract-domain "jane@test.org")
;; => "test.org"
```

### Example 3
```clojure
(extract-domain "bob@company.co.uk")
;; => "company.co.uk"
```

### Example 4
```clojure
(extract-domain "invalid-email")
;; => ""
```

## Tips

- Use `clojure.string/split` to split the string by @
- `split` returns a vector of parts
- The domain is the second element (index 1)
- Remember to require `[clojure.string :as str]`
- Handle the case where @ is not present (return empty string)
- You can use `last` to get the last part after splitting

## Testing your solution

```bash
cd challenges/012-extract-domain/
clj -M solution.clj
```
