# 074 - GraphQL Response Builder

**Level**: 15/18
**Type**: Adapter
**Concepts**: Selective field inclusion, Nested field selection, GraphQL patterns

## Context

GraphQL allows clients to request exactly the fields they need. A response builder constructs responses including only requested fields, reducing bandwidth and improving performance by not sending unnecessary data.

## Objective

Implement an adapter that builds GraphQL responses based on field selection, conditionally including nested fields.

## Specification

### Input

- `data` (map): Complete data
- `fields` (vector): Requested field selectors
  - Simple: `:name`
  - Nested: `{:posts [:title :date]}`

### Output

- (map): Response with only requested fields

### Rules

- Include only fields present in `fields` vector
- For nested selectors (maps), recursively apply to nested data
- If field is a collection, apply selection to each item
- Omit unrequested fields entirely

## Examples

```clojure
(build-response {:id "U1" :name "John" :email "john@example.com" :age 30}
                [:id :name])
;; => {:id "U1" :name "John"}

(build-response {:id "U1" :name "John" :posts [{:id "P1" :title "Post 1" :date "2024-01-15" :body "..."}]}
                [:name {:posts [:title :date]}])
;; => {:name "John" :posts [{:title "Post 1" :date "2024-01-15"}]}
```

## Tips

- Process each field selector
- Use `map?` to detect nested selectors
- For collections, use `mapv` to apply selection to each
- Build result map incrementally

## Testing your solution

```bash
cd challenges/074-graphql-response-builder/
clj -M solution.clj
```
