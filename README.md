# 90 Desafios de Clojure em 90 Dias

Um projeto de aprendizado progressivo de Clojure, do básico aos padrões profissionais, inspirado em código de produção real.

## 🎯 Visão Geral

Este projeto contém **90 desafios de Clojure** organizados em **18 níveis de dificuldade** (5 desafios por nível), projetados para levar você de iniciante absoluto a padrões de código profissional usados em produção.

### Por que este projeto é diferente?

Todos os desafios são inspirados em **código real de produção** encontrado na pasta `references/`. Você não vai apenas aprender sintaxe Clojure — vai aprender **como escrever código Clojure profissional** usado em sistemas reais.

## 📚 Estrutura e Organização

### Níveis de Dificuldade

- **18 níveis** de complexidade crescente
- **5 desafios por nível** = 90 desafios no total
- Progressão: **iniciante absoluto** (nível 1) → **padrões enterprise** (nível 18)

### Três Tipos de Código

Cada nível contém desafios dos três tipos fundamentais de código encontrados em aplicações reais:

#### 1. **Pure Functions** 🔵
Funções puras que sempre retornam o mesmo resultado para a mesma entrada.
```clojure
(defn maior-de-idade? [idade]
  (>= idade 18))
```

#### 2. **Adapters** 🟡
Transformações entre diferentes representações de dados (domínio ↔ API externa).
```clojure
(defn user-wire->domain [{:keys [name age]}]
  {:nome name :idade age})
```

#### 3. **Controllers** 🟢
Orquestração de fluxos e casos de uso (buscar → validar → processar → retornar).
```clojure
(defn buscar-usuario [db user-id]
  (if (= (:id db) user-id)
    {:status :sucesso :usuario db}
    {:status :erro :mensagem "Usuário não encontrado"}))
```

### Distribuição dos Desafios

Cada nível possui desafios dos **3 tipos**, com complexidade crescente em todos eles:

```
Nível 1:  Pure Function simples + Adapter básico + Controller trivial
Nível 2:  Pure Function simples + Adapter básico + Controller trivial
...
Nível 9:  Pure Function média + Adapter média + Controller média
Nível 10: Pure Function média + Adapter média + Controller média
...
Nível 18: Pure Function complexa + Adapter complexo + Controller complexo
```

## 📈 Progressão de Complexidade

### Pure Functions (🔵)

| Níveis | Complexidade | Conceitos |
|--------|--------------|-----------|
| 1-3 | Básico | Predicados simples (`=`, `and`, `or`) |
| 4-6 | Fundamentos | Transformações (`map`, `filter`, `reduce`) |
| 7-9 | Intermediário | String processing, parsing |
| 10-12 | Avançado | Destructuring, composição de funções |
| 13-15 | Profissional | Pattern matching (`cond`, `case`) |
| 16-18 | Enterprise | Lógica condicional complexa, múltiplos branches |

### Adapters (🟡)

| Níveis | Complexidade | Conceitos |
|--------|--------------|-----------|
| 1-3 | Básico | Mapeamento direto (`assoc`, `dissoc`) |
| 4-6 | Fundamentos | Transformação de chaves |
| 7-9 | Intermediário | Coerção de tipos (string ↔ número, keyword) |
| 10-12 | Avançado | Estruturas aninhadas, destructuring |
| 13-15 | Profissional | Transformações bidirecionais |
| 16-18 | Enterprise | Query string parsing, conditional field inclusion |

### Controllers (🟢)

| Níveis | Complexidade | Conceitos |
|--------|--------------|-----------|
| 1-3 | Básico | Validação simples, retorno direto |
| 4-6 | Fundamentos | Sequências de operações (fetch → validate → return) |
| 7-9 | Intermediário | Error handling básico |
| 10-12 | Avançado | Composição de múltiplas funções |
| 13-15 | Profissional | Threading macros (`->`, `->>`) |
| 16-18 | Enterprise | Fluxos transacionais complexos |

## 🗂️ Estrutura de um Desafio

Cada desafio está em sua própria pasta e contém dois arquivos:

```
challenges/001-maior-de-idade/
├── README.md      # Descrição do problema, exemplos, requisitos
└── solution.clj   # Solução + explicações detalhadas
```

### Template do README.md

```markdown
# [Número] - [Nome do Desafio]

**Nível**: X/18
**Tipo**: [Pure Function | Adapter | Controller]
**Conceitos**: [lista de conceitos Clojure abordados]

## Contexto
[Descrição do problema em linguagem simples]

## Objetivo
[O que deve ser implementado]

## Especificação

### Entrada
[Descrição dos parâmetros, com tipos]

### Saída
[Descrição do retorno, com tipo]

### Regras
- [Regra 1]
- [Regra 2]

## Exemplos

### Exemplo 1
```clojure
(nome-funcao entrada)
;; => saída esperada
```

## Dicas
- [Dica 1 sobre funções úteis]
- [Dica 2 sobre approach]

## Testando sua solução
[Como rodar e testar]
```

### Template do solution.clj

```clojure
;; =============================================================================
;; [NÚMERO] - [NOME DO DESAFIO]
;; Nível: X/18 | Tipo: [tipo]
;; =============================================================================

;; EXPLICAÇÃO DA SOLUÇÃO
;; ----------------------
;; [Parágrafo explicando o approach escolhido]

(ns challenge-XXX.solution)

;; IMPLEMENTAÇÃO
;; -------------

(defn nome-funcao
  "Docstring explicando o que a função faz"
  [param1 param2]
  ;; Comentário explicando o passo
  (let [resultado (operacao param1)]
    ;; Comentário explicando o retorno
    resultado))

;; EXPLICAÇÃO DOS CONCEITOS
;; -------------------------
;;
;; 1. [Conceito Clojure #1]
;;    [Explicação detalhada]
;;
;; 2. [Conceito Clojure #2]
;;    [Explicação detalhada]

;; PADRÃO DAS REFERÊNCIAS
;; ----------------------
;; Este desafio é inspirado em: [referência específica]
;; Padrão usado: [descrição do padrão]

;; EXEMPLOS DE USO
;; ---------------

(comment
  ;; Exemplo 1
  (nome-funcao input1)
  ;; => output1

  ;; Exemplo 2
  (nome-funcao input2)
  ;; => output2
)

;; TESTES
;; ------

(defn -test []
  (assert (= (nome-funcao input1) expected1))
  (assert (= (nome-funcao input2) expected2))
  (println "✓ Todos os testes passaram!"))

;; Execute: (-test)
```

## 🚀 Como Usar os Desafios

### Pré-requisitos

- **Clojure instalado** (versão 1.11 ou superior)
- **Editor de texto** ou IDE (recomendado: VS Code + Calva, IntelliJ + Cursive, Emacs + CIDER)

### Rodando um Desafio

#### Opção 1: Online (sem instalação)
```
1. Visite https://replit.com/languages/clojure
2. Cole o código do desafio
3. Execute e teste
```

#### Opção 2: Local com Clojure CLI
```bash
cd challenges/001-maior-de-idade/
clj -M solution.clj
```

#### Opção 3: REPL interativo
```bash
clj
(load-file "challenges/001-maior-de-idade/solution.clj")
(-test)
```

### Abordagem Recomendada

1. 📖 **Leia o README.md** do desafio para entender o problema
2. 💻 **Tente implementar** sua própria solução
3. 🔍 **Compare** com a solução oficial em `solution.clj`
4. 📚 **Leia as explicações** dos conceitos
5. 🧪 **Experimente variações** no REPL

## 📖 Exemplos de Desafios por Nível

### Nível 1: Fundamentos Básicos

#### 🔵 Pure Function: Verificar Maioridade
```clojure
(maior-de-idade? 18) ;; => true
(maior-de-idade? 17) ;; => false

;; Implementação
(defn maior-de-idade?
  "Verifica se a idade é >= 18"
  [idade]
  (>= idade 18))
```

#### 🟡 Adapter: Transformar Usuário Wire→Domain
```clojure
(user-wire->domain {:name "João" :age 25})
;; => {:nome "João" :idade 25}

;; Implementação
(defn user-wire->domain
  "Adapta formato externo (inglês) para domínio (português)"
  [{:keys [name age]}]
  {:nome name
   :idade age})
```

#### 🟢 Controller: Buscar Usuário com Validação
```clojure
(buscar-usuario {:id 1 :nome "Ana"} 1)
;; => {:status :sucesso :usuario {:id 1 :nome "Ana"}}

(buscar-usuario {:id 1 :nome "Ana"} 999)
;; => {:status :erro :mensagem "Usuário não encontrado"}

;; Implementação
(defn buscar-usuario
  "Busca usuário por ID e retorna resultado com status"
  [db user-id]
  (if (= (:id db) user-id)
    {:status :sucesso :usuario db}
    {:status :erro :mensagem "Usuário não encontrado"}))
```

---

### Nível 10: Intermediário

#### 🔵 Pure Function: Extrair Domínio de Email
```clojure
(extrair-dominio "user@example.com")
;; => {:valido? true :dominio "example.com"}

(extrair-dominio "invalid-email")
;; => {:valido? false :dominio nil}
```

#### 🟡 Adapter: API→Domain com Coerção
```clojure
(api->pedido {:order_id "123" :total "99.90" :created "2024-01-15"})
;; => {:pedido-id 123 :total 99.90 :criado-em #inst "2024-01-15"}
```

#### 🟢 Controller: Criar Pedido com Validações
```clojure
(criar-pedido db {:itens [] :total 0})
;; => {:status :erro :razao :pedido-vazio}

(criar-pedido db {:itens [{:id 1}] :total 10})
;; => {:status :sucesso :pedido-id 456}
```

---

### Nível 18: Padrões Enterprise

#### 🔵 Pure Function: Análise de Risco Complexa
```clojure
(analisar-risco {:idade 17 :score 800 :historico :bom})
;; => {:aprovado? false :razao :menor-de-idade}

(analisar-risco {:idade 25 :score 300 :historico :ruim})
;; => {:aprovado? false :razao :score-baixo}

(analisar-risco {:idade 25 :score 800 :historico :bom})
;; => {:aprovado? true :razao :analise-automatizada}
```

#### 🟡 Adapter: Query String Parser Bidirecional
```clojure
(query-string->map "name=João&age=25&city=São Paulo")
;; => {:name "João" :age "25" :city "São Paulo"}

(map->query-string {:name "João" :age 25 :cidade nil})
;; => "name=João&age=25" ;; ignora campos nil
```

#### 🟢 Controller: Fluxo Transacional Completo
```clojure
(processar-transacao contexto {:valor 1000 :tipo :pix})
;; Valida saldo → Verifica limites → Aplica regras de negócio
;; → Registra transação → Publica evento
;; => {:status :sucesso :transacao-id "abc-123" :novo-saldo 500}
```

## 🔗 Inspiração das Referências

Os desafios são baseados em código real de produção:

### Pure Functions (`references/pure-functions/`)
- **exemplo1.md**: Predicados de validação e extração
- **exemplo2.md**: Composição de funções e transformações
- **exemplo3.md**: Lógica condicional complexa (cond com múltiplos branches)
- **exemplo4.md**: Validadores simples com `and`/`or`
- **exemplo5.md**: Transformações de domínio financeiro

### Adapters (`references/adapters/`)
- **exemplo1/exemplo2.md**: Schemas de entrada/saída
- **exemplo3.md**: Transformações complexas (OAuth, query strings, JSON)
- **exemplo4.md**: Mapeamento direto com destructuring
- **exemplo5.md**: Coerção de tipos bidirecional

### Controllers (`references/controllers/`)
- **exemplo1.md**: CRUD simples com validação
- **exemplo2.md**: Orquestração complexa (OAuth, múltiplas funções)
- **exemplo3.md**: Fluxos com cálculos atrasados
- **exemplo5.md**: Transações com versionamento e eventos

## 📊 Progressão de Aprendizado

### Fase 1: Fundamentos (Níveis 1-6)
**O que você vai aprender:**
- ✅ Sintaxe básica de Clojure
- ✅ Funções puras e imutabilidade
- ✅ Estruturas de dados (mapas, vetores)
- ✅ Funções de primeira classe
- ✅ Transformações básicas (map, filter, reduce)

### Fase 2: Intermediário (Níveis 7-12)
**O que você vai aprender:**
- ✅ Destructuring avançado
- ✅ Threading macros (`->`, `->>`)
- ✅ Composição de funções
- ✅ Error handling
- ✅ String processing e parsing

### Fase 3: Avançado (Níveis 13-18)
**O que você vai aprender:**
- ✅ Pattern matching complexo
- ✅ Transformações multi-estágio
- ✅ Fluxos transacionais
- ✅ Padrões arquiteturais
- ✅ Código production-ready

## 🧰 Recursos Adicionais

### Documentação Oficial
- [Clojure.org](https://clojure.org/) - Documentação oficial
- [ClojureDocs](https://clojuredocs.org/) - Exemplos práticos de funções

### Funções Úteis por Categoria

#### Predicados
`=` `not=` `>` `<` `>=` `<=` `nil?` `empty?` `contains?` `some?` `every?`

#### Transformação de Coleções
`map` `filter` `reduce` `mapv` `filterv` `into` `take` `drop` `partition`

#### Manipulação de Mapas
`assoc` `dissoc` `get` `get-in` `update` `update-in` `merge` `select-keys` `keys` `vals`

#### Strings (require [clojure.string :as str])
`str/split` `str/trim` `str/upper-case` `str/lower-case` `str/replace` `str/join`

#### Lógica Condicional
`and` `or` `not` `if` `when` `cond` `case` `if-let` `when-let` `some->`

#### Threading Macros
`->` `->>` `some->` `some->>` `as->` `cond->` `cond->>`

### Dicas de Estudo

1. **📚 Comece do nível 1**: Mesmo que já saiba Clojure, os primeiros níveis estabelecem os padrões usados em todo o projeto

2. **🧪 Experimente no REPL**: Clojure é uma linguagem interativa. Teste cada parte da sua solução no REPL

3. **📖 Leia as soluções**: Entenda o "porquê" de cada decisão, não só o "como" implementar

4. **🔄 Tente variações**: Após resolver um desafio, experimente outras abordagens

5. **🔗 Compare com referências**: Veja como o código real em `references/` usa esses mesmos padrões

6. **⏱️ Um desafio por dia**: O ideal é resolver um desafio por dia para consolidar o aprendizado (90 dias total)

## 🎓 Requisitos

### O que você precisa saber antes de começar?
- **Nada!** Os desafios começam do absoluto zero

### O que você NÃO precisa?
- ❌ Bibliotecas externas: todos os desafios usam apenas `clojure.core`
- ❌ Configuração complexa: basta ter Clojure instalado
- ❌ Experiência prévia: feito para iniciantes

## 📁 Estrutura do Projeto

```
tlt-clojure-90-challenges-90-days/
├── CLAUDE.md                           # Este arquivo (guia completo)
├── README.md                           # Visão geral do projeto
├── challenges/
│   ├── 001-maior-de-idade/             # Nível 1 - Pure Function
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 002-usuario-wire-domain/        # Nível 1 - Adapter
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 003-buscar-usuario/             # Nível 1 - Controller
│   │   ├── README.md
│   │   └── solution.clj
│   ├── 004-XXX/                        # Nível 2 - Pure Function
│   ├── 005-XXX/                        # Nível 2 - Adapter
│   ├── 006-XXX/                        # Nível 2 - Controller
│   ├── ...
│   ├── 088-XXX/                        # Nível 18 - Pure Function
│   ├── 089-XXX/                        # Nível 18 - Adapter
│   └── 090-fluxo-transacional/         # Nível 18 - Controller
│       ├── README.md
│       └── solution.clj
└── references/
    ├── controllers/                    # Código real de controllers
    │   ├── exemplo1.md
    │   ├── exemplo2.md
    │   ├── exemplo3.md
    │   └── exemplo5.md
    ├── adapters/                       # Código real de adapters
    │   ├── exemplo1.md
    │   ├── exemplo2.md
    │   ├── exemplo3.md
    │   ├── exemplo4.md
    │   └── exemplo5.md
    └── pure-functions/                 # Código real de pure functions
        ├── exemplo1.md
        ├── exemplo2.md
        ├── exemplo3.md
        ├── exemplo4.md
        └── exemplo5.md
```

## 🎯 Objetivos de Aprendizado

Ao completar os 90 desafios, você será capaz de:

- ✅ **Escrever código Clojure idiomático** seguindo best practices
- ✅ **Aplicar padrões profissionais** usados em produção
- ✅ **Estruturar aplicações** com separação clara de responsabilidades
- ✅ **Transformar dados** entre diferentes representações
- ✅ **Orquestrar fluxos** complexos de forma legível
- ✅ **Raciocinar funcionalmente** sobre problemas
- ✅ **Ler e entender código** Clojure de projetos reais

## 🤝 Contribuindo

Este projeto está em constante evolução. Sugestões de novos desafios ou melhorias são bem-vindas!

---

**Pronto para começar?** 🚀

Comece pelo [desafio 001](challenges/001-maior-de-idade/) e avance no seu ritmo. Cada desafio foi projetado para ensinar um conceito específico enquanto prepara você para padrões profissionais de Clojure!

**Boa jornada de aprendizado!** 🎉
