# Challenge Creation Guide
## 90 Clojure Challenges - Comprehensive Guidelines for AI & Developers

---

## Table of Contents
1. [Introduction](#introduction)
2. [Language Requirements](#language-requirements)
3. [Challenge Structure](#challenge-structure)
4. [Pattern Library from References](#pattern-library-from-references)
5. [Challenge Templates](#challenge-templates)
6. [Complexity Guidelines](#complexity-guidelines)
7. [Quality Checklist](#quality-checklist)
8. [Complete Example](#complete-example)
9. [Creation Workflow](#creation-workflow)
10. [Reference Mapping Summary](#reference-mapping-summary)

---

## Introduction

### Purpose of This Document

This document serves as the **definitive guide** for creating all 90 Clojure challenges in this project. It provides:

- **Detailed patterns** extracted from real production code (`references/` folder)
- **Step-by-step guidelines** for challenge creation
- **Templates and examples** for consistency
- **Complexity progression** across 18 difficulty levels
- **Quality standards** for educational content

### Who Should Use This Guide

- **AI assistants** (like Claude) creating challenges
- **Developers** contributing new challenges to the project
- **Educators** adapting challenges for teaching
- **Reviewers** ensuring challenge quality and consistency

### How to Use This Guide

1. **Understand the pattern library** - Study the patterns from `references/`
2. **Choose appropriate complexity** - Match the level (1-18) to pattern complexity
3. **Follow the templates** - Use provided README.md and solution.clj structures
4. **Check quality criteria** - Validate against the checklist before finalizing
5. **Maintain consistency** - Ensure all challenges follow the same style

---

## Language Requirements

### Critical Language Rules

**EVERYTHING MUST BE IN ENGLISH** - This includes:

- ✅ README.md - All text and descriptions
- ✅ solution.clj - All comments, docstrings, explanations
- ✅ **Function names** - Use English names (e.g., `adult?`, `find-user`, `process-transaction`)
- ✅ **Keywords** - Use English keywords (`:status`, `:success`, `:error`, `:approved`)
- ✅ **Domain data** - Use English in examples (`{:name "John" :age 25}`)
- ✅ **Variable names** - All parameters and locals in English
- ✅ **Test descriptions** - Assert messages in English
- ✅ **File/folder names** - English names (`challenges/001-check-adult/`)

### Example of Correct Language Usage

```clojure
;; This function checks if a user is of legal age (18 years or older).
;; It's a simple predicate that returns a boolean value.

(defn adult?
  "Checks if a person is of legal age (18+ years)"
  [age]
  (>= age 18))

;; Test
(adult? 18)  ;; => true
{:name "John" :age 25 :status :active}

```

---

## Challenge Structure

### Directory Organization

Each challenge follows this structure:

```
challenges/
└── XXX-descriptive-name/
    ├── README.md      # Problem description (English)
    └── solution.clj   # Solution + explanations (English)
```

### Naming Conventions

**Folder Names:**
- Format: `NNN-short-description`
- Examples: `001-check-adult`, `045-parse-email`, `090-transactional-flow`
- Use kebab-case, keep it concise, **all in English**

**Function Names (in solutions):**
- Use English: `adult?`, `user-wire->domain`, `find-user`
- Follow Clojure conventions: predicates end with `?`, conversions use `->`
- All English, no Portuguese

---

## Pattern Library from References

This section maps patterns from the `references/` directory to challenge levels. While the reference code uses Portuguese, **all challenges must be created in English**.

### Pure Function Patterns

#### Level 1-3: Simple Predicates

**Pattern**: Boolean checks with `=`, `and`, `or`

**Reference**: `references/pure-functions/exemplo4.md`
```clojure
;; Reference uses Portuguese, but challenges use English
;; Reference: (s/defn risk-allows-automation? ...)
;; Challenge: (defn risk-allows-automation? ...)
```

**Challenge Ideas (all in English):**
1. Check if user is of legal age (`adult?` with `>= 18`)
2. Validate if email contains `@` symbol (`valid-email?`)
3. Check if balance is positive (`positive-balance?`)
4. Verify if name is not empty (`has-name?`)
5. Test if number is even (`even-number?`)

**Complexity**: 1-3 lines, single condition or simple boolean logic

---

#### Level 4-6: Basic Transformations

**Pattern**: `map`, `filter`, `reduce` on collections

**Reference**: `references/pure-functions/exemplo2.md`

**Challenge Ideas (all in English):**
1. Filter valid emails from a list (`filter-valid-emails`)
2. Sum all positive numbers (`sum-positive`)
3. Transform list of names to uppercase (`uppercase-names`)
4. Extract even numbers from list (`extract-evens`)
5. Count words in list of strings (`count-words`)

**Complexity**: 3-7 lines, use of `map`/`filter`/`reduce`

---

#### Level 7-9: String Processing

**Pattern**: String manipulation and parsing

**Reference**: `references/pure-functions/exemplo2.md`

**Challenge Ideas (all in English):**
1. Parse email to extract username and domain (`parse-email`)
2. Split full name into first and last name (`split-name`)
3. Extract numbers from mixed string (`extract-numbers`)
4. Validate and parse phone number (`parse-phone`)
5. Clean and normalize URLs (`normalize-url`)

**Complexity**: 7-12 lines, string functions, parsing logic

---

#### Level 10-12: Destructuring & Composition

**Pattern**: Complex destructuring, function composition

**Reference**: `references/pure-functions/exemplo1.md`

**Challenge Ideas (all in English):**
1. Extract nested data from user profile (`extract-profile-data`)
2. Compose multiple validation functions (`validate-all`)
3. Chain transformations with `comp` (`transform-pipeline`)
4. Destructure and rebuild complex map (`rebuild-user`)
5. Extract specific fields from nested structure (`extract-nested`)

**Complexity**: 10-15 lines, destructuring patterns, function composition

---

#### Level 13-15: Pattern Matching

**Pattern**: `cond`, `case` with multiple branches

**Reference**: `references/pure-functions/exemplo3.md`

**Challenge Ideas (all in English):**
1. Risk scoring with multiple conditions (`calculate-risk`)
2. State machine transitions (`next-state`)
3. Complex business rule evaluation (`evaluate-rules`)
4. Multi-criteria decision maker (`make-decision`)
5. Eligibility checker with various rules (`check-eligibility`)

**Complexity**: 15-25 lines, `cond` with 3-5 branches

---

#### Level 16-18: Complex Conditional Logic

**Pattern**: Multiple `cond` branches, nested logic

**References**: `references/pure-functions/exemplo3.md`, `exemplo5.md`

**Challenge Ideas (all in English):**
1. Multi-step approval workflow (`approve-request`)
2. Complex eligibility checker (`complex-eligibility`)
3. Transaction validation (`validate-transaction`)
4. Fraud detection system (`detect-fraud`)
5. Authorization flow (`authorize-action`)

**Complexity**: 25+ lines, `cond` with 6+ branches, nested conditions

---

### Adapter Patterns

#### Level 1-3: Direct Mapping

**Pattern**: `assoc`, `dissoc`, simple key renaming

**Reference**: `references/adapters/exemplo4.md`

**Challenge Ideas (all in English):**
1. Wire format → Domain format (`wire->domain`)
2. Extract subset of fields (`extract-fields`)
3. Add computed field to map (`add-computed-field`)
4. Rename keys in map (`rename-keys`)
5. Remove sensitive fields (`remove-sensitive`)

**Example**:
```clojure
;; Input: {:first-name "John" :last-name "Doe" :email "john@example.com"}
;; Output: {:name "John Doe" :email "john@example.com"}
```

**Complexity**: Direct key mapping (2-5 fields)

---

#### Level 4-6: Key Transformation

**Pattern**: Transforming key naming conventions

**Reference**: `references/adapters/exemplo3.md`

**Challenge Ideas (all in English):**
1. kebab-case → camelCase converter (`->camel-case`)
2. snake_case → kebab-case converter (`->kebab-case`)
3. Transform all keys to lowercase (`lowercase-keys`)
4. Convert string keys to keywords (`->keyword-keys`)
5. Normalize key formats (`normalize-keys`)

**Example**:
```clojure
;; Input: {:first-name "John" :last-name "Doe"}
;; Output: {:firstName "John" :lastName "Doe"}
```

**Complexity**: Key transformation (5-8 fields)

---

#### Level 7-9: Type Coercion

**Pattern**: String ↔ number, keyword ↔ string

**Reference**: `references/adapters/exemplo5.md`

**Challenge Ideas (all in English):**
1. Parse string dates → date objects (`parse-date`)
2. Convert string numbers → numbers (`parse-number`)
3. Keywords ↔ Strings (`keyword->string`, `string->keyword`)
4. Boolean string parsing (`parse-boolean`)
5. Timestamp conversion (`parse-timestamp`)

**Example**:
```clojure
;; Input: {:amount "99.50" :date "2024-01-15" :status "active"}
;; Output: {:amount 99.50 :date #inst "2024-01-15" :status :active}
```

**Complexity**: Type coercion (3-5 types)

---

#### Level 10-12: Nested Structures

**Pattern**: Nested destructuring, deep transformations

**Reference**: `references/adapters/exemplo1.md`, `exemplo2.md`

**Challenge Ideas (all in English):**
1. Transform nested user profile (`transform-profile`)
2. Flatten nested data structure (`flatten-data`)
3. Extract from deeply nested JSON (`extract-deep`)
4. Transform nested order (`transform-order`)
5. Restructure API response (`restructure-response`)

**Example**:
```clojure
;; Input: {:user {:name "John" :address {:city "NYC" :zip "10001"}}}
;; Output: {:name "John" :city "NYC" :zip "10001"}
```

**Complexity**: Nested maps (2-3 levels deep)

---

#### Level 13-15: Bidirectional Transformations

**Pattern**: A → B and B → A transformations

**Reference**: `references/adapters/exemplo5.md`

**Challenge Ideas (all in English):**
1. Domain ↔ Database format (`domain->db`, `db->domain`)
2. Internal ↔ API representation (`internal->api`, `api->internal`)
3. Request ↔ Response transformation
4. Encrypted ↔ Decrypted data
5. Compressed ↔ Expanded format

**Example**:
```clojure
;; Direction 1: domain->db
{:full-name "John Doe" :email "john@example.com"}
;; => {:name "John Doe" :contact_email "john@example.com"}

;; Direction 2: db->domain
{:name "John Doe" :contact_email "john@example.com"}
;; => {:full-name "John Doe" :email "john@example.com"}
```

**Complexity**: Bidirectional (both directions)

---

#### Level 16-18: Complex Parsing

**Pattern**: Query strings, JSON, conditional fields

**Reference**: `references/adapters/exemplo3.md`

**Challenge Ideas (all in English):**
1. Query string parser (`parse-query-string`)
2. JSON with conditional fields (`conditional-json`)
3. Multi-format adapter (`parse-format`)
4. Parse complex headers (`parse-headers`)
5. URL encoder/decoder (`encode-url`, `decode-url`)

**Example**:
```clojure
;; Input: "name=John&age=25&city=NYC"
;; Output: {:name "John" :age "25" :city "NYC"}
```

**Complexity**: Complex formats, regex, edge cases

---

### Controller Patterns

#### Level 1-3: Simple Validation

**Pattern**: Validate → Return result

**Reference**: `references/controllers/exemplo1.md`

**Challenge Ideas (all in English):**
1. Fetch user by ID (`fetch-user`)
2. Check if item exists (`check-exists`)
3. Simple CRUD: create user (`create-user`)
4. Validate input format (`validate-input`)
5. Lookup by ID with default (`lookup-or-default`)

**Example**:
```clojure
(defn fetch-user [db user-id]
  (or (get db user-id)
      {:status :error :message "User not found"}))
```

**Complexity**: 1-2 operations

---

#### Level 4-6: Operation Sequences

**Pattern**: Fetch → Validate → Transform → Return

**Reference**: `references/controllers/exemplo1.md`

**Challenge Ideas (all in English):**
1. Fetch, validate, format user (`process-user`)
2. Get order, check status, calculate (`process-order`)
3. Retrieve, transform, cache (`cache-data`)
4. Multi-step retrieval (`multi-step-fetch`)
5. Fetch and enrich data (`enrich-data`)

**Complexity**: 3-4 operations in sequence

---

#### Level 7-9: Error Handling

**Pattern**: `or` composition, explicit error handling

**Reference**: `references/controllers/exemplo1.md`

**Challenge Ideas (all in English):**
1. Try with fallback (`try-with-fallback`)
2. Validate with errors (`validate-with-errors`)
3. Handle multiple errors (`handle-errors`)
4. Chain with error propagation (`chain-operations`)
5. Retry with error handling (`retry-on-error`)

**Complexity**: Error handling with `or`/`if`

---

#### Level 10-12: Function Composition

**Pattern**: Multiple helper functions

**Reference**: `references/controllers/exemplo2.md`

**Challenge Ideas (all in English):**
1. OAuth flow (`oauth-flow`)
2. Payment pipeline (`payment-pipeline`)
3. Multi-stage validation (`validate-stages`)
4. User registration (`register-user`)
5. Order processing (`process-order`)

**Complexity**: 3-5 composed functions

---

#### Level 13-15: Threading Macros

**Pattern**: `->`, `->>` for data flow

**Reference**: `references/controllers/exemplo5.md`

**Challenge Ideas (all in English):**
1. Data pipeline with `->` (`transform-pipeline`)
2. Chain validations with `->>` (`validation-chain`)
3. Build object step-by-step (`build-object`)
4. Filter and transform (`filter-transform`)
5. Request processing (`process-request`)

**Example**:
```clojure
(-> user
    (assoc :timestamp (now))
    (validate-user)
    (save-to-db)
    (send-notification))
```

**Complexity**: Threading macros with 3-5 steps

---

#### Level 16-18: Complex Transactional Flows

**Pattern**: Feature flags, multiple services, events

**Reference**: `references/controllers/exemplo2.md`, `exemplo5.md`

**Challenge Ideas (all in English):**
1. Transaction with events (`process-transaction`)
2. Multi-service orchestration (`orchestrate-services`)
3. Feature-flagged workflow (`feature-workflow`)
4. Complex approval flow (`approval-flow`)
5. Transactional saga (`saga-transaction`)

**Complexity**: Multiple services, feature flags, events

---

## Challenge Templates

### README.md Template

**All content in English:**

```markdown
# [Number] - [Challenge Name]

**Level**: X/18
**Type**: [Pure Function | Adapter | Controller]
**Concepts**: [Clojure concepts covered]

## Context

[2-3 sentences providing real-world context in English]

## Objective

[Clear objective statement in English]

## Specification

### Input

- `param-name` ([type]): [English description]
- `another-param` ([type]): [English description]

### Output

- ([return-type]): [English description]

### Rules

- [Rule 1 in English]
- [Rule 2 in English]
- [Rule 3 in English]

## Examples

### Example 1
```clojure
(function-name input-1)
;; => expected-output-1
```

### Example 2
```clojure
(function-name input-2)
;; => expected-output-2
```

### Example 3
```clojure
(function-name edge-case)
;; => edge-output
```

## Tips

- [English tip 1]
- [English tip 2]
- [English tip 3]

## Testing your solution

```bash
cd challenges/XXX-challenge-name/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-XXX.solution)
(challenge-XXX.solution/-test)
```
```

---

### solution.clj Template

**All content in English:**

```clojure
;; =============================================================================
;; [NUMBER] - [CHALLENGE NAME]
;; Level: X/18 | Type: [Type]
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; [English explanation of the approach, 2-4 paragraphs discussing:
;;  - Why this approach
;;  - Key Clojure features used
;;  - Trade-offs considered
;;  - Real-world relevance]

(ns challenge-XXX.solution
  (:require [clojure.string :as str])) ;; English requires

;; IMPLEMENTATION
;; --------------

(defn function-name
  "English docstring explaining what the function does.

  Parameters:
  - param1: English description
  - param2: English description

  Returns: English description of return value"
  [param1 param2]
  ;; English comment explaining this step
  (let [result (operation param1)]
    ;; English comment explaining final transformation
    (final-operation result param2)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. [Concept Name]
;;    [Detailed English explanation, 2-4 sentences covering:
;;     - What the concept is
;;     - Why it's useful
;;     - When to use it
;;     - Common pitfalls]
;;
;; 2. [Another Concept]
;;    [English explanation]
;;
;; 3. [Third Concept]
;;    [English explanation]

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/[category]/[file].md
;;
;; Pattern used: [English description of the pattern]
;;
;; Real-world usage: [English explanation with example from reference]

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: [English description]
  (function-name input1)
  ;; => output1

  ;; Example 2: [English description]
  (function-name input2)
  ;; => output2

  ;; Example 3: Edge case - [English description]
  (function-name edge-input)
  ;; => edge-output
)

;; TESTS
;; -----

(defn -test []
  (assert (= (function-name test1) expected1)
          "English test description 1")
  (assert (= (function-name test2) expected2)
          "English test description 2")
  (assert (= (function-name edge-case) edge-expected)
          "English edge case description")
  (println "✓ All tests passed!"))

;; Run the tests
;; Execute in REPL: (-test)
```

---

## Complexity Guidelines

### Pure Functions Complexity Ladder

| Level | Lines | Characteristics | Operations |
|-------|-------|-----------------|------------|
| 1-3 | 1-3 | Single operation | `>=`, `and`, `or` |
| 4-6 | 3-7 | Collection ops | `map`, `filter`, `reduce` |
| 7-9 | 7-12 | String processing | `split`, `trim`, `replace` |
| 10-12 | 10-15 | Destructuring | `let`, `comp`, `->` |
| 13-15 | 15-25 | Pattern matching | `cond` 3-5 branches |
| 16-18 | 25+ | Complex logic | `cond` 6+ branches |

### Adapters Complexity Ladder

| Level | Fields | Characteristics | Operations |
|-------|--------|-----------------|------------|
| 1-3 | 2-5 | Direct mapping | `assoc`, `dissoc` |
| 4-6 | 5-8 | Key transformation | `update-keys` |
| 7-9 | 3-5 types | Type coercion | String→Number |
| 10-12 | 2-3 levels | Nested structures | `get-in`, `assoc-in` |
| 13-15 | Both ways | Bidirectional | A→B and B→A |
| 16-18 | Complex | Parsing | Query strings, JSON |

### Controllers Complexity Ladder

| Level | Operations | Characteristics | Patterns |
|-------|-----------|-----------------|----------|
| 1-3 | 1-2 | Simple validation | Fetch + validate |
| 4-6 | 3-4 | Sequential | Fetch → validate → transform |
| 7-9 | 2-3 | Error handling | `or` composition |
| 10-12 | 3-5 | Composition | Helper functions |
| 13-15 | 3-5 steps | Threading | `->`, `->>` |
| 16-18 | Multiple | Orchestration | Services, events |

---

## Quality Checklist

### Technical Quality ✅

- [ ] Uses only `clojure.core`
- [ ] Idiomatic Clojure style
- [ ] Appropriate complexity for level
- [ ] Comprehensive tests
- [ ] Well-commented in English
- [ ] Functions are pure (for Pure Functions)
- [ ] Proper error handling (for Controllers)

### Educational Quality ✅

- [ ] Teaches specific concept
- [ ] Builds on previous challenges
- [ ] Clear problem statement in English
- [ ] Detailed solution explanation in English
- [ ] Links to reference
- [ ] Thorough concept explanations
- [ ] Meaningful examples

### Documentation Quality ✅

- [ ] README.md complete in English
- [ ] solution.clj thorough in English
- [ ] Examples demonstrate concepts
- [ ] Tips are helpful
- [ ] Professional English throughout
- [ ] Consistent formatting
- [ ] No typos or errors

### Language Requirements ✅

- [ ] **README.md all in English**
- [ ] **solution.clj comments all in English**
- [ ] **Docstrings all in English**
- [ ] **Function names in English** (e.g., `adult?`, not `maior-de-idade?`)
- [ ] **Keywords in English** (`:status`, `:success`, not `:sucesso`)
- [ ] **Variable names in English** (`age`, not `idade`)
- [ ] **Test data in English** (`{:name "John"}`, not `{:nome "João"}`)

---

## Complete Example

Full example of Level 1 Pure Function challenge **completely in English**:

### File: `challenges/001-check-adult/README.md`

```markdown
# 001 - Check if Adult

**Level**: 1/18
**Type**: Pure Function
**Concepts**: Predicates, Comparison operators, Boolean values

## Context

In many systems, we need to verify if a person is of legal age (18 years or older) to grant access to certain features or content. This is a fundamental validation in age-restricted services, financial applications, and content platforms.

## Objective

Implement a pure function that checks if a given age represents an adult (18 years or older).

## Specification

### Input

- `age` (integer): The person's age in years

### Output

- (boolean): `true` if age >= 18, `false` otherwise

### Rules

- Legal age is defined as 18 years or older
- Function must be pure (same input = same output)
- No side effects allowed

## Examples

### Example 1
```clojure
(adult? 18)
;; => true
```

### Example 2
```clojure
(adult? 17)
;; => false
```

### Example 3
```clojure
(adult? 25)
;; => true
```

### Example 4
```clojure
(adult? 0)
;; => false
```

## Tips

- Use the `>=` comparison operator
- Comparison operators return boolean values directly
- The function should be a simple one-liner
- Think about the boolean nature of comparisons

## Testing your solution

```bash
cd challenges/001-check-adult/
clj -M solution.clj
```
Then in REPL:
```clojure
(require 'challenge-001.solution)
(challenge-001.solution/-test)
```
```

### File: `challenges/001-check-adult/solution.clj`

```clojure
;; =============================================================================
;; 001 - CHECK IF ADULT
;; Level: 1/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This is a simple predicate function that compares age with the legal
;; threshold of 18. Since comparison operators in Clojure return boolean
;; values directly, we can simply return the result of the >= operation.
;;
;; This approach is preferred over using an if statement because it's more
;; concise and idiomatic. The >= operator already returns exactly what we need
;; (true or false), so there's no reason for additional conditional logic.
;;
;; This pattern is common in production code for simple boolean checks,
;; as seen in reference examples where risk ratings and validations
;; are performed using direct comparison operations.

(ns challenge-001.solution)

;; IMPLEMENTATION
;; --------------

(defn adult?
  "Checks if a person is of legal age (18 years or older).

  Parameters:
  - age: The person's age in years (integer)

  Returns: Boolean - true if age >= 18, false otherwise"
  [age]
  ;; Compare age with legal threshold of 18
  ;; Returns true if age >= 18, false otherwise
  (>= age 18))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Predicates in Clojure
;;    By convention, predicate functions (those returning booleans)
;;    end with a question mark (?). This naming makes code more
;;    readable by clearly indicating boolean returns.
;;    Examples: empty?, nil?, even?, odd?, contains?, adult?
;;
;; 2. Comparison Operators
;;    Clojure has standard comparison operators for numbers:
;;    - =  (equal)
;;    - <  (less than)
;;    - >  (greater than)
;;    - <= (less than or equal)
;;    - >= (greater than or equal)
;;    - not= (not equal)
;;    All return boolean values (true or false).
;;
;; 3. Pure Functions
;;    This function is pure because it satisfies three requirements:
;;    a) Same input always produces same output
;;       - (adult? 18) will ALWAYS return true
;;       - (adult? 17) will ALWAYS return false
;;    b) No side effects - doesn't modify anything outside scope
;;       - No printing, no I/O, no global state changes
;;    c) Doesn't depend on external state
;;       - Only uses input parameter
;;
;;    Pure functions are easier to test, reason about, and parallelize.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo4.md
;;
;; Pattern used: Simple boolean predicate with comparison operator
;;
;; Real-world usage: The reference code uses this pattern to check if
;; risk rating allows automated processing:
;;   (and (= risk-rating :low) (= risk-reason :fast-analysis-queue))
;;
;; This shows how simple predicates are building blocks for complex
;; business logic in production systems.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Exactly legal age
  (adult? 18)
  ;; => true

  ;; Example 2: One year below legal age
  (adult? 17)
  ;; => false

  ;; Example 3: Well above legal age
  (adult? 25)
  ;; => true

  ;; Example 4: Edge case - newborn
  (adult? 0)
  ;; => false

  ;; Example 5: Edge case - very old
  (adult? 100)
  ;; => true

  ;; Example 6: Teenage years
  (adult? 16)
  ;; => false
)

;; TESTS
;; -----

(defn -test []
  (assert (= (adult? 18) true)
          "Should return true for age 18 (exactly legal)")
  (assert (= (adult? 17) false)
          "Should return false for age 17 (below legal)")
  (assert (= (adult? 25) true)
          "Should return true for age 25 (above legal)")
  (assert (= (adult? 0) false)
          "Edge case - should return false for age 0")
  (assert (= (adult? 100) true)
          "Edge case - should return true for age 100")
  (assert (= (adult? 16) false)
          "Should return false for teenager age 16")
  (assert (= (adult? 19) true)
          "Should return true for age 19 (just above)")
  (println "✓ All tests passed! The adult? function works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
```

---

## Creation Workflow

Follow these steps when creating each challenge:

### 1. Choose Pattern from References
- Identify level (1-18)
- Consult Pattern Library section
- Select appropriate pattern
- Read reference file thoroughly

### 2. Define Learning Objective
- What Clojure concept to teach?
- Ensure it builds on previous challenges
- Verify appropriate for level

### 3. Create Problem Statement
- Write clear context (2-3 sentences) **in English**
- State objective in one sentence **in English**
- Define inputs/outputs precisely **in English**
- List rules and constraints **in English**

### 4. Implement Solution
- Write solution following template
- Add thorough comments **in English**
- Include detailed explanation **in English**
- Use **English** function names and variables
- Use **English** keywords (`:status`, `:success`)
- Use **English** in all example data (`{:name "John"}`)

### 5. Write Comprehensive Tests
- Cover normal cases
- Cover edge cases
- Cover error cases
- Add descriptive messages **in English**
- Aim for 5-7 test cases minimum

### 6. Add Concept Explanations
- Explain 2-3 Clojure concepts **in English**
- For each: what, why, when, gotchas
- All explanations **in English**

### 7. Link to Reference
- Cite specific reference file
- Quote/describe exact pattern
- Explain real-world usage **in English**

### 8. Review Complexity
- Verify matches expected level
- Check against Complexity Guidelines
- Adjust if needed

### 9. Validate English Quality
- Proofread all text
- Check grammar, spelling
- Ensure professional tone
- Verify technical accuracy
- **Everything must be in English**

### 10. Final Quality Check
- Run through complete Quality Checklist
- Test the solution works
- Verify all sections filled
- Ensure consistency
- **Confirm all English, no Portuguese**

---

## Reference Mapping Summary

Quick guide for selecting patterns:

### Pure Functions

| Level | Pattern | Reference | Example |
|-------|---------|-----------|---------|
| 1-3 | Simple predicates | exemplo4.md | `adult?` |
| 4-6 | Collections | exemplo2.md | `filter-valid` |
| 7-9 | String processing | exemplo2.md | `parse-email` |
| 10-12 | Destructuring | exemplo1.md | `extract-data` |
| 13-15 | Pattern matching | exemplo3.md | `calculate-risk` |
| 16-18 | Complex logic | exemplo3.md, exemplo5.md | `validate-transaction` |

### Adapters

| Level | Pattern | Reference | Example |
|-------|---------|-----------|---------|
| 1-3 | Direct mapping | exemplo4.md | `wire->domain` |
| 4-6 | Key transform | exemplo3.md | `->camel-case` |
| 7-9 | Type coercion | exemplo5.md | `parse-number` |
| 10-12 | Nested | exemplo1.md | `flatten-data` |
| 13-15 | Bidirectional | exemplo5.md | `domain<->db` |
| 16-18 | Complex parsing | exemplo3.md | `parse-query-string` |

### Controllers

| Level | Pattern | Reference | Example |
|-------|---------|-----------|---------|
| 1-3 | Simple validation | exemplo1.md | `fetch-user` |
| 4-6 | Sequences | exemplo1.md | `process-order` |
| 7-9 | Error handling | exemplo1.md | `try-operation` |
| 10-12 | Composition | exemplo2.md | `oauth-flow` |
| 13-15 | Threading | exemplo5.md | `transform-pipeline` |
| 16-18 | Complex flows | exemplo2.md, exemplo5.md | `process-transaction` |

---

## Final Notes

### Critical Requirements

- ✅ **Everything in English** - Functions, keywords, variables, data, comments
- ✅ **No Portuguese** - Anywhere in challenge code or documentation
- ✅ **Quality over quantity** - One excellent challenge beats five mediocre ones
- ✅ **Educational value** - Every challenge teaches something valuable
- ✅ **Real-world relevance** - Connect patterns to production code
- ✅ **Progressive difficulty** - Each level should feel achievable yet challenging
- ✅ **Consistency** - Follow templates strictly

### When in Doubt

- Refer to reference files
- Check existing challenges
- Review Complete Example section
- Consult Quality Checklist
- **Remember: ALL ENGLISH, NO PORTUGUESE**

---

**This guide will evolve as the project grows. Suggestions welcome!**
