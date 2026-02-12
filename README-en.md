# 90 Clojure Challenges in 90 Days

A progressive Clojure learning project, from basics to professional patterns, inspired by real production code.

## 🎯 Overview

This project contains **90 Clojure challenges** organized into **18 difficulty levels** (5 challenges per level), designed to take you from absolute beginner to professional code patterns used in production.

### Why is this project different?

All challenges are inspired by **real production code** found in the `references/` folder. You won't just learn Clojure syntax — you'll learn **how to write professional Clojure code** used in real systems.

## 📚 Structure and Organization

### Difficulty Levels

- **18 levels** of increasing complexity
- **5 challenges per level** = 90 challenges in total
- Progression: **absolute beginner** (level 1) → **enterprise patterns** (level 18)

### Three Types of Code

Each level contains challenges of the three fundamental code types found in real applications:

#### 1. **Pure Functions** 🔵
Pure functions that always return the same result for the same input.
```clojure
(defn maior-de-idade? [idade]
  (>= idade 18))
```

#### 2. **Adapters** 🟡
Transformations between different data representations (domain ↔ external API).
```clojure
(defn user-wire->domain [{:keys [name age]}]
  {:nome name :idade age})
```

#### 3. **Controllers** 🟢
Orchestration of flows and use cases (fetch → validate → process → return).
```clojure
(defn buscar-usuario [db user-id]
  (if (= (:id db) user-id)
    {:status :sucesso :usuario db}
    {:status :erro :mensagem "Usuário não encontrado"}))
```

### Challenge Distribution

Each level has challenges of all **3 types**, with increasing complexity in all of them:

```
Level 1:  Simple Pure Function + Basic Adapter + Trivial Controller
Level 2:  Simple Pure Function + Basic Adapter + Trivial Controller
...
Level 9:  Medium Pure Function + Medium Adapter + Medium Controller
Level 10: Medium Pure Function + Medium Adapter + Medium Controller
...
Level 18: Complex Pure Function + Complex Adapter + Complex Controller
```

## 📈 Complexity Progression

### Pure Functions (🔵)

| Levels | Complexity | Concepts |
|--------|------------|----------|
| 1-3 | Basic | Simple predicates (`=`, `and`, `or`) |
| 4-6 | Fundamentals | Transformations (`map`, `filter`, `reduce`) |
| 7-9 | Intermediate | String processing, parsing |
| 10-12 | Advanced | Destructuring, function composition |
| 13-15 | Professional | Pattern matching (`cond`, `case`) |
| 16-18 | Enterprise | Complex conditional logic, multiple branches |

### Adapters (🟡)

| Levels | Complexity | Concepts |
|--------|------------|----------|
| 1-3 | Basic | Direct mapping (`assoc`, `dissoc`) |
| 4-6 | Fundamentals | Key transformation |
| 7-9 | Intermediate | Type coercion (string ↔ number, keyword) |
| 10-12 | Advanced | Nested structures, destructuring |
| 13-15 | Professional | Bidirectional transformations |
| 16-18 | Enterprise | Query string parsing, conditional field inclusion |

### Controllers (🟢)

| Levels | Complexity | Concepts |
|--------|------------|----------|
| 1-3 | Basic | Simple validation, direct return |
| 4-6 | Fundamentals | Operation sequences (fetch → validate → return) |
| 7-9 | Intermediate | Basic error handling |
| 10-12 | Advanced | Multiple function composition |
| 13-15 | Professional | Threading macros (`->`, `->>`) |
| 16-18 | Enterprise | Complex transactional flows |

## 🗂️ Challenge Structure

Each challenge is in its own folder and contains two files:

```
challenges/001-maior-de-idade/
├── README.md      # Problem description, examples, requirements
└── solution.clj   # Solution + detailed explanations
```

### README.md Template

```markdown
# [Number] - [Challenge Name]

**Level**: X/18
**Type**: [Pure Function | Adapter | Controller]
**Concepts**: [list of Clojure concepts covered]

## Context
[Problem description in simple language]

## Objective
[What should be implemented]

## Specification

### Input
[Parameter descriptions, with types]

### Output
[Return description, with type]

### Rules
- [Rule 1]
- [Rule 2]

## Examples

### Example 1
```clojure
(function-name input)
;; => expected output
```

## Tips
- [Tip 1 about useful functions]
- [Tip 2 about approach]

## Testing your solution
[How to run and test]
```

### solution.clj Template

```clojure
;; =============================================================================
;; [NUMBER] - [CHALLENGE NAME]
;; Level: X/18 | Type: [type]
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; [Paragraph explaining the chosen approach]

(ns challenge-XXX.solution)

;; IMPLEMENTATION
;; --------------

(defn function-name
  "Docstring explaining what the function does"
  [param1 param2]
  ;; Comment explaining the step
  (let [result (operation param1)]
    ;; Comment explaining the return
    result))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. [Clojure Concept #1]
;;    [Detailed explanation]
;;
;; 2. [Clojure Concept #2]
;;    [Detailed explanation]

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: [specific reference]
;; Pattern used: [pattern description]

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1
  (function-name input1)
  ;; => output1

  ;; Example 2
  (function-name input2)
  ;; => output2
)

;; TESTS
;; -----

(defn -test []
  (assert (= (function-name input1) expected1))
  (assert (= (function-name input2) expected2))
  (println "✓ All tests passed!"))

;; Run: (-test)
```

## 🚀 How to Use the Challenges

### Prerequisites

- **Clojure installed** (version 1.11 or higher)
- **Text editor** or IDE (recommended: VS Code + Calva, IntelliJ + Cursive, Emacs + CIDER)

### Running a Challenge

#### Option 1: Online (no installation)
```
1. Visit https://replit.com/languages/clojure
2. Paste the challenge code
3. Run and test
```

#### Option 2: Local with Clojure CLI
```bash
cd challenges/001-maior-de-idade/
clj -M 001.clj
```

#### Option 3: Interactive REPL
```bash
clj
(load-file "challenges/001-maior-de-idade/001.clj")
(-test)
```

### Recommended Approach

1. 📖 **Read the README.md** of the challenge to understand the problem
2. 💻 **Try to implement** your own solution
3. 🔍 **Compare** with the official solution in `solution.clj`
4. 📚 **Read the explanations** of the concepts
5. 🧪 **Experiment with variations** in the REPL

## 📖 Examples of Challenges by Level

### Level 1: Basic Fundamentals

#### 🔵 Pure Function: Check Legal Age
```clojure
(maior-de-idade? 18) ;; => true
(maior-de-idade? 17) ;; => false

;; Implementation
(defn maior-de-idade?
  "Checks if age is >= 18"
  [idade]
  (>= idade 18))
```

#### 🟡 Adapter: Transform User Wire→Domain
```clojure
(user-wire->domain {:name "João" :age 25})
;; => {:nome "João" :idade 25}

;; Implementation
(defn user-wire->domain
  "Adapts external format (English) to domain (Portuguese)"
  [{:keys [name age]}]
  {:nome name
   :idade age})
```

#### 🟢 Controller: Fetch User with Validation
```clojure
(buscar-usuario {:id 1 :nome "Ana"} 1)
;; => {:status :sucesso :usuario {:id 1 :nome "Ana"}}

(buscar-usuario {:id 1 :nome "Ana"} 999)
;; => {:status :erro :mensagem "Usuário não encontrado"}

;; Implementation
(defn buscar-usuario
  "Fetches user by ID and returns result with status"
  [db user-id]
  (if (= (:id db) user-id)
    {:status :sucesso :usuario db}
    {:status :erro :mensagem "Usuário não encontrado"}))
```

---

### Level 10: Intermediate

#### 🔵 Pure Function: Extract Email Domain
```clojure
(extrair-dominio "user@example.com")
;; => {:valido? true :dominio "example.com"}

(extrair-dominio "invalid-email")
;; => {:valido? false :dominio nil}
```

#### 🟡 Adapter: API→Domain with Coercion
```clojure
(api->pedido {:order_id "123" :total "99.90" :created "2024-01-15"})
;; => {:pedido-id 123 :total 99.90 :criado-em #inst "2024-01-15"}
```

#### 🟢 Controller: Create Order with Validations
```clojure
(criar-pedido db {:itens [] :total 0})
;; => {:status :erro :razao :pedido-vazio}

(criar-pedido db {:itens [{:id 1}] :total 10})
;; => {:status :sucesso :pedido-id 456}
```

---

### Level 18: Enterprise Patterns

#### 🔵 Pure Function: Complex Risk Analysis
```clojure
(analisar-risco {:idade 17 :score 800 :historico :bom})
;; => {:aprovado? false :razao :menor-de-idade}

(analisar-risco {:idade 25 :score 300 :historico :ruim})
;; => {:aprovado? false :razao :score-baixo}

(analisar-risco {:idade 25 :score 800 :historico :bom})
;; => {:aprovado? true :razao :analise-automatizada}
```

#### 🟡 Adapter: Bidirectional Query String Parser
```clojure
(query-string->map "name=João&age=25&city=São Paulo")
;; => {:name "João" :age "25" :city "São Paulo"}

(map->query-string {:name "João" :age 25 :cidade nil})
;; => "name=João&age=25" ;; ignores nil fields
```

#### 🟢 Controller: Complete Transactional Flow
```clojure
(processar-transacao contexto {:valor 1000 :tipo :pix})
;; Validates balance → Checks limits → Applies business rules
;; → Records transaction → Publishes event
;; => {:status :sucesso :transacao-id "abc-123" :novo-saldo 500}
```

## 🔗 Inspiration from References

The challenges are based on real production code:

### Pure Functions (`references/pure-functions/`)
- **exemplo1.md**: Validation and extraction predicates
- **exemplo2.md**: Function composition and transformations
- **exemplo3.md**: Complex conditional logic (cond with multiple branches)
- **exemplo4.md**: Simple validators with `and`/`or`
- **exemplo5.md**: Financial domain transformations

### Adapters (`references/adapters/`)
- **exemplo1/exemplo2.md**: Input/output schemas
- **exemplo3.md**: Complex transformations (OAuth, query strings, JSON)
- **exemplo4.md**: Direct mapping with destructuring
- **exemplo5.md**: Bidirectional type coercion

### Controllers (`references/controllers/`)
- **exemplo1.md**: Simple CRUD with validation
- **exemplo2.md**: Complex orchestration (OAuth, multiple functions)
- **exemplo3.md**: Flows with delayed computations
- **exemplo5.md**: Transactions with versioning and events

## 📊 Learning Progression

### Phase 1: Fundamentals (Levels 1-6)
**What you'll learn:**
- ✅ Basic Clojure syntax
- ✅ Pure functions and immutability
- ✅ Data structures (maps, vectors)
- ✅ First-class functions
- ✅ Basic transformations (map, filter, reduce)

### Phase 2: Intermediate (Levels 7-12)
**What you'll learn:**
- ✅ Advanced destructuring
- ✅ Threading macros (`->`, `->>`)
- ✅ Function composition
- ✅ Error handling
- ✅ String processing and parsing

### Phase 3: Advanced (Levels 13-18)
**What you'll learn:**
- ✅ Complex pattern matching
- ✅ Multi-stage transformations
- ✅ Transactional flows
- ✅ Architectural patterns
- ✅ Production-ready code

## 🧰 Additional Resources

### Official Documentation
- [Clojure.org](https://clojure.org/) - Official documentation
- [ClojureDocs](https://clojuredocs.org/) - Practical function examples

### Useful Functions by Category

#### Predicates
`=` `not=` `>` `<` `>=` `<=` `nil?` `empty?` `contains?` `some?` `every?`

#### Collection Transformation
`map` `filter` `reduce` `mapv` `filterv` `into` `take` `drop` `partition`

#### Map Manipulation
`assoc` `dissoc` `get` `get-in` `update` `update-in` `merge` `select-keys` `keys` `vals`

#### Strings (require [clojure.string :as str])
`str/split` `str/trim` `str/upper-case` `str/lower-case` `str/replace` `str/join`

#### Conditional Logic
`and` `or` `not` `if` `when` `cond` `case` `if-let` `when-let` `some->`

#### Threading Macros
`->` `->>` `some->` `some->>` `as->` `cond->` `cond->>`

### Study Tips

1. **📚 Start from level 1**: Even if you already know Clojure, the first levels establish the patterns used throughout the project

2. **🧪 Experiment in the REPL**: Clojure is an interactive language. Test each part of your solution in the REPL

3. **📖 Read the solutions**: Understand the "why" behind each decision, not just the "how" to implement

4. **🔄 Try variations**: After solving a challenge, experiment with other approaches

5. **🔗 Compare with references**: See how the real code in `references/` uses these same patterns

6. **⏱️ One challenge per day**: Ideally solve one challenge per day to consolidate learning (90 days total)

## 🎓 Requirements

### What do you need to know before starting?
- **Nothing!** The challenges start from absolute zero

### What do you NOT need?
- ❌ External libraries: all challenges use only `clojure.core`
- ❌ Complex setup: just need Clojure installed
- ❌ Prior experience: made for beginners

## 📁 Project Structure

```
tlt-clojure-90-challenges-90-days/
├── CLAUDE.md                           # Complete guide (Portuguese)
├── README.md                           # Project overview (Portuguese)
├── README-en.md                        # Project overview (English)
├── challenges/
│   ├── 001-maior-de-idade/             # Level 1 - Pure Function
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 002-usuario-wire-domain/        # Level 1 - Adapter
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 003-buscar-usuario/             # Level 1 - Controller
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 004-XXX/                        # Level 2 - Pure Function
│   ├── 005-XXX/                        # Level 2 - Adapter
│   ├── 006-XXX/                        # Level 2 - Controller
│   ├── ...
│   ├── 088-XXX/                        # Level 18 - Pure Function
│   ├── 089-XXX/                        # Level 18 - Adapter
│   └── 090-fluxo-transacional/         # Level 18 - Controller
│       ├── README.md
│       └── solution.clj
└── references/
    ├── controllers/                    # Real controller code
    │   ├── exemplo1.md
    │   ├── exemplo2.md
    │   ├── exemplo3.md
    │   └── exemplo5.md
    ├── adapters/                       # Real adapter code
    │   ├── exemplo1.md
    │   ├── exemplo2.md
    │   ├── exemplo3.md
    │   ├── exemplo4.md
    │   └── exemplo5.md
    └── pure-functions/                 # Real pure function code
        ├── exemplo1.md
        ├── exemplo2.md
        ├── exemplo3.md
        ├── exemplo4.md
        └── exemplo5.md
```

## 🎯 Learning Objectives

By completing the 90 challenges, you will be able to:

- ✅ **Write idiomatic Clojure code** following best practices
- ✅ **Apply professional patterns** used in production
- ✅ **Structure applications** with clear separation of concerns
- ✅ **Transform data** between different representations
- ✅ **Orchestrate complex flows** in a readable way
- ✅ **Think functionally** about problems
- ✅ **Read and understand** Clojure code from real projects

## 🤝 Contributing

This project is constantly evolving. Suggestions for new challenges or improvements are welcome!

---

**Ready to start?** 🚀

Begin with [challenge 001](challenges/001-maior-de-idade/) and progress at your own pace. Each challenge is designed to teach a specific concept while preparing you for professional Clojure patterns!

**Happy learning journey!** 🎉
