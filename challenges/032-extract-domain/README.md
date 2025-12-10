# 032 - Extract Domain from URL

**Level**: 7/18
**Type**: Pure Function
**Concepts**: String processing, URL parsing, Pattern matching

## Context

When analyzing web traffic, logging requests, or processing links, we often need to extract the domain name from full URLs. This involves stripping away the protocol (http/https), path, query parameters, and port numbers to get just the domain name.

## Objective

Implement a pure function that extracts the domain name from a URL string, handling various URL formats.

## Specification

### Input

- `url` (string): A URL string (may include protocol, path, query params)

### Output

- (string): The domain name extracted from the URL

### Rules

- Remove protocol if present (`http://`, `https://`)
- Remove path and everything after domain (starting with `/`)
- Remove port number if present (`:8080`, `:3000`, etc.)
- Remove query parameters (starting with `?`)
- Handle URLs with or without protocol
- Function must be pure

## Examples

### Example 1
```clojure
(extract-domain "https://www.example.com/path/to/page")
;; => "www.example.com"
```

### Example 2
```clojure
(extract-domain "http://api.github.com:443/users?page=1")
;; => "api.github.com"
```

### Example 3
```clojure
(extract-domain "example.com/about")
;; => "example.com"
```

### Example 4
```clojure
(extract-domain "https://localhost:8080")
;; => "localhost"
```

## Tips

- Use `clojure.string/replace` to remove protocols
- Use `clojure.string/split` with multiple delimiters
- Process the URL step by step: protocol → domain → path
- Consider edge cases like URLs without protocol
- The `->` threading macro can make the transformations readable

## Testing your solution

```bash
cd challenges/032-extract-domain/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-032.solution)
(challenge-032.solution/-test)
```
