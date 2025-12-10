# 085 - Sophisticated Authorization

**Level**: 17/18
**Type**: Pure Function
**Concepts**: ABAC (Attribute-Based Access Control), Policy engine, Context evaluation, Complex rules

## Context

Modern authorization systems go beyond simple role-based access (RBAC) to attribute-based access control (ABAC), where decisions depend on user attributes, resource attributes, environmental context, and complex policies. This is essential for multi-tenant SaaS platforms, healthcare systems (HIPAA), financial services, and enterprise applications requiring fine-grained access control.

## Objective

Implement an ABAC authorization system that evaluates complex policies based on user attributes, resource attributes, environmental context, and hierarchical rules.

## Specification

### Input

- `policy` (map): Authorization policy with rules
- `user` (map): User with attributes (roles, department, level, etc.)
- `resource` (map): Resource being accessed with attributes
- `action` (keyword): Action being performed (:read, :write, :delete, :admin)
- `context` (map): Environmental context (time, location, device, etc.)

### Output

- (map): Authorization decision with:
  - `:allow` (boolean): Whether action is allowed
  - `:reason` (string): Explanation of decision
  - `:matched-rules` (vector): Rules that matched
  - `:conditions` (vector): Any conditional requirements

### Rules

- Evaluate all applicable rules in priority order
- Support: role-based, attribute-based, time-based, location-based rules
- Allow hierarchical rules (organization → department → team)
- Support conditions (allow if MFA completed, allow during business hours)
- Provide detailed audit trail of decision
- Handle conflicting rules with explicit precedence

## Examples

### Example 1: Role-based access
```clojure
(authorize
  {:rules [{:id "R1" :roles #{:admin} :actions #{:read :write :delete} :allow true}]}
  {:user-id "U123" :roles #{:admin}}
  {:id "DOC-456" :type :document}
  :delete
  {})
;; => {:allow true
;;     :reason "Rule R1: Admin role grants full access"
;;     :matched-rules ["R1"]}
```

### Example 2: Attribute-based with conditions
```clojure
(authorize
  {:rules [{:id "R2"
            :conditions [(fn [user resource ctx]
                          (and (= (:department user) (:owner-dept resource))
                               (< (:hour ctx) 18)))]
            :actions #{:read :write}
            :allow true}]}
  {:user-id "U123" :department "engineering"}
  {:id "DOC-789" :owner-dept "engineering"}
  :write
  {:hour 14})
;; => {:allow true
;;     :reason "Rule R2: Department match + business hours"
;;     :matched-rules ["R2"]}
```

### Example 3: Denied with reason
```clojure
(authorize
  {:rules [{:id "R3" :roles #{:admin} :actions #{:delete} :allow true}]}
  {:user-id "U456" :roles #{:user}}
  {:id "DOC-999"}
  :delete
  {})
;; => {:allow false
;;     :reason "No matching rules for action :delete"
;;     :matched-rules []}
```

## Tips

- Evaluate rules from most specific to most general
- Cache authorization decisions for performance
- Use predicates for complex conditions
- Log all authorization decisions for audit
- Consider policy versioning for compliance
- Support policy testing and simulation

## Testing your solution

```bash
cd challenges/085-sophisticated-authorization/
clj -M solution.clj
```
Then in the REPL:
```clojure
(require 'challenge-085.solution)
(challenge-085.solution/-test)
```
