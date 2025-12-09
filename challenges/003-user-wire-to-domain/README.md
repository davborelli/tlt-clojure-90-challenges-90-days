# 003 - User Wire to Domain

**Level**: 1/18
**Type**: Adapter
**Concepts**: Map manipulation, assoc, dissoc, Key renaming

## Context

In web applications, external API responses often use different naming conventions than our internal domain models. Adapters transform data between these representations, ensuring consistency throughout the application.

## Objective

Implement an adapter function that transforms a user map from wire format (external API) to domain format (internal).

## Specification

### Input

- `user-wire` (map): A map with keys `:firstName`, `:lastName`, `:emailAddress`

### Output

- (map): A map with keys `:first-name`, `:last-name`, `:email`

### Rules

- Transform camelCase keys to kebab-case
- Rename `:emailAddress` to `:email`
- Preserve all values unchanged
- Function must be pure

## Examples

### Example 1
```clojure
(wire->domain {:firstName "John" :lastName "Doe" :emailAddress "john@example.com"})
;; => {:first-name "John" :last-name "Doe" :email "john@example.com"}
```

### Example 2
```clojure
(wire->domain {:firstName "Jane" :lastName "Smith" :emailAddress "jane@example.com"})
;; => {:first-name "Jane" :last-name "Smith" :email "jane@example.com"}
```

### Example 3
```clojure
(wire->domain {:firstName "Bob" :lastName "Johnson" :emailAddress "bob@test.com"})
;; => {:first-name "Bob" :last-name "Johnson" :email "bob@test.com"}
```

## Tips

- Use destructuring with `:keys` to extract values from the input map
- Build the output map using literal map syntax `{:key value}`
- Remember that kebab-case uses hyphens, not underscores
- Test with different names to ensure it works for all inputs

## Testing your solution

```bash
cd challenges/003-user-wire-to-domain/
clj -M solution.clj
```
