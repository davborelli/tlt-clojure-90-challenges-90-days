# 073 - XML to JSON Converter

**Level**: 15/18
**Type**: Adapter
**Concepts**: Format conversion, Nested structure parsing, Attribute handling

## Context

Legacy systems often use XML while modern APIs prefer JSON. Converting between formats requires handling nested elements, attributes, and text content while preserving the data structure.

## Objective

Implement an adapter that converts simplified XML maps to JSON-friendly maps.

## Specification

### Input

- `xml-data` (map): Simplified XML representation
  ```clojure
  {:tag :element-name
   :attrs {...}
   :content [{:tag :child ...} "text" ...]}
  ```

### Output

- (map): JSON-friendly representation
  ```clojure
  {:elementName {...}
   :attributes {...}
   :children [...]}
  ```

### Rules

- Convert tag names: kebab-case → camelCase
- Extract attributes to `:attributes`
- Process content:
  - Child elements → recursively convert
  - Text strings → collect in `:text`
- Handle empty content/attributes gracefully

## Examples

```clojure
(xml->json {:tag :user-profile :attrs {:id "123"} :content [{:tag :name :attrs {} :content ["John"]} {:tag :email :attrs {} :content ["john@example.com"]}]})
;; => {:userProfile {:attributes {:id "123"} :children [{:name {:text "John"}} {:email {:text "john@example.com"}}]}}
```

## Tips

- Use recursion for nested elements
- Convert kebab-case: split by `-`, capitalize rest, join
- Filter content by type (map vs string)
- Base case: empty content

## Testing your solution

```bash
cd challenges/073-xml-to-json/
clj -M solution.clj
```
