# 028 - Bidirectional Transformation (Domain ↔ Wire)

**Level**: 6/18
**Type**: Adapter
**Concepts**: Bidirectional transformation, Inverse operations, Data layer boundaries

## Context

When building APIs, you need transformations in both directions: domain model → wire format (for responses) and wire format → domain model (for requests). Creating both transforms ensures clean separation between internal and external representations.

## Objective

Implement two adapter functions: `domain->wire` that converts internal domain format to external API format, and `wire->domain` that converts back.

## Specification

### Domain Format
```clojure
{:user-id 123 :full-name "John Doe" :email-address "john@example.com"}
```

### Wire Format
```clojure
{"userId" 123 "fullName" "John Doe" "emailAddress" "john@example.com"}
```

### Rules

- `domain->wire`: kebab-case keywords → camelCase strings
- `wire->domain`: camelCase strings → kebab-case keywords
- Preserve all values unchanged
- Functions must be inverses: `(domain->wire (wire->domain x))` = `x`

## Examples

### Example 1
```clojure
(domain->wire {:user-id 123 :full-name "John" :email-address "j@test.com"})
;; => {"userId" 123 "fullName" "John" "emailAddress" "j@test.com"}
```

### Example 2
```clojure
(wire->domain {"userId" 456 "fullName" "Jane" "emailAddress" "jane@test.com"})
;; => {:user-id 456 :full-name "Jane" :email-address "jane@test.com"}
```

## Tips

- For domain->wire: Convert `:user-id` → `"userId"`
- For wire->domain: Convert `"userId"` → `:user-id`
- Use `reduce-kv` to rebuild map with transformed keys
- CamelCase: capitalize first letter of each word except first
- kebab-case: lowercase with hyphens

## Testing your solution

```bash
cd challenges/028-bidirectional-transform/
clj -M solution.clj
```
