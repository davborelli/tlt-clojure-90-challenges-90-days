# 072 - Authorization Flow

**Level**: 15/18
**Type**: Pure Function
**Concepts**: Complex authorization, Role-based access, Resource permissions, Hierarchical rules

## Context

Authorization systems determine if a user can perform an action on a resource based on roles, ownership, resource state, and organizational hierarchy. A user might access their own resources, resources in their team, or admin-level resources depending on their role.

## Objective

Implement a complex authorization function that evaluates multiple permission rules based on roles, ownership, and context.

## Specification

### Input

- `user` (map): `{:user-id "..." :role :... :team-id "..."}`
  - Roles: `:admin`, `:manager`, `:member`, `:guest`
- `resource` (map): `{:resource-id "..." :owner-id "..." :team-id "..." :visibility :... :status :...}`
  - Visibility: `:public`, `:team`, `:private`
  - Status: `:active`, `:archived`
- `action` (keyword): `:read`, `:write`, `:delete`, `:admin`

### Output

- (map): `{:authorized boolean :reason "..."}`

### Rules

**Admin role:**
- Can perform any action on any resource

**Manager role:**
- Can perform any action on team resources (same team-id)
- Can read public resources
- Cannot access other teams' resources

**Member role:**
- Can read/write own resources
- Can read team resources (same team-id) if visibility :team or :public
- Can read public resources
- Cannot delete resources (except own)
- Cannot perform admin actions

**Guest role:**
- Can only read public resources

**Additional rules:**
- Cannot modify archived resources (except admins)
- Ownership grants full access (read/write/delete but not admin)

## Examples

```clojure
(authorize {:user-id "U1" :role :admin :team-id "T1"}
           {:resource-id "R1" :owner-id "U2" :team-id "T2" :visibility :private :status :active}
           :delete)
;; => {:authorized true :reason "Admin access granted"}

(authorize {:user-id "U1" :role :member :team-id "T1"}
           {:resource-id "R1" :owner-id "U1" :visibility :private :status :active}
           :write)
;; => {:authorized true :reason "Owner access granted"}

(authorize {:user-id "U1" :role :guest :team-id nil}
           {:resource-id "R1" :owner-id "U2" :team-id "T1" :visibility :private :status :active}
           :read)
;; => {:authorized false :reason "Insufficient permissions"}
```

## Tips

- Use cond to check rules in order: admin → ownership → role-based
- Check archived status early
- Team access requires matching team-ids
- Build clear reason strings for audit trails

## Testing your solution

```bash
cd challenges/072-authorization-flow/
clj -M solution.clj
```
