;; =============================================================================
;; 085 - SOPHISTICATED AUTHORIZATION
;; Level: 17/18 | Type: Pure Function
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This function implements ABAC (Attribute-Based Access Control), a sophisticated
;; authorization model that goes beyond simple role checks. ABAC evaluates decisions
;; based on attributes of the user, resource, action, and environmental context.
;;
;; The challenge is evaluating complex rules with multiple conditions that can
;; reference any combination of attributes. Each rule has: an ID (for audit),
;; optional role requirements, optional attribute conditions (predicates),
;; actions it applies to, and whether it allows or denies access.
;;
;; We iterate through rules in order (priority), checking if each rule matches
;; the current request. The first matching rule determines the authorization.
;; This mirrors production authorization systems in SaaS platforms, healthcare
;; (HIPAA), and financial services where fine-grained access control is critical.

(ns challenge-085.solution)

;; HELPER FUNCTIONS
;; ----------------

(defn rule-matches-role?
  "Checks if user has any of the required roles.

  Parameters:
  - user: User map with :roles set
  - rule: Rule map with optional :roles set

  Returns: Boolean - true if user has matching role or rule has no role requirement"
  [user rule]
  (if-let [required-roles (:roles rule)]
    (some (:roles user) required-roles)
    true)) ; No role requirement = match

(defn rule-matches-action?
  "Checks if action is allowed by rule.

  Parameters:
  - action: Action keyword being performed
  - rule: Rule map with :actions set

  Returns: Boolean - true if action is in rule's allowed actions"
  [action rule]
  (contains? (:actions rule) action))

(defn rule-conditions-pass?
  "Evaluates all conditions for a rule.

  Parameters:
  - user: User map
  - resource: Resource map
  - context: Context map
  - rule: Rule map with optional :conditions vector of predicate functions

  Returns: Boolean - true if all conditions pass or no conditions exist"
  [user resource context rule]
  (if-let [conditions (:conditions rule)]
    (every? (fn [condition-fn]
              (condition-fn user resource context))
            conditions)
    true)) ; No conditions = pass

(defn rule-matches?
  "Checks if a rule matches the authorization request.

  Parameters:
  - user, resource, action, context: Request parameters
  - rule: Authorization rule

  Returns: Boolean - true if rule applies to this request"
  [user resource action context rule]
  (and (rule-matches-role? user rule)
       (rule-matches-action? action rule)
       (rule-conditions-pass? user resource context rule)))

;; MAIN IMPLEMENTATION
;; -------------------

(defn authorize
  "Evaluates ABAC authorization policies.

  Checks rules in priority order. First matching rule determines authorization.
  Rules can specify: roles, actions, custom conditions (predicates).
  Returns detailed decision with matched rules and explanation.

  Parameters:
  - policy: Map with :rules vector
  - user: User map with :user-id, :roles, and other attributes
  - resource: Resource map with :id and other attributes
  - action: Keyword action being performed
  - context: Environmental context (time, location, device, etc.)

  Returns: Map with :allow, :reason, :matched-rules, :conditions"
  [policy user resource action context]
  (let [rules (:rules policy)

        ;; Find first matching rule
        matching-rule (first (filter (fn [rule]
                                       (rule-matches? user resource action context rule))
                                     rules))]

    (if matching-rule
      ;; Rule matched - check if it allows or denies
      (let [allow? (:allow matching-rule)
            rule-id (:id matching-rule)]
        {:allow allow?
         :reason (if allow?
                   (str "Rule " rule-id ": Access granted")
                   (str "Rule " rule-id ": Access denied"))
         :matched-rules [rule-id]
         :conditions (or (:required-conditions matching-rule) [])})

      ;; No matching rule - deny by default
      {:allow false
       :reason (str "No matching rules for action " action)
       :matched-rules []
       :conditions []})))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. ABAC (Attribute-Based Access Control)
;;    Unlike RBAC (Role-Based) which only checks roles, ABAC evaluates attributes:
;;    - User attributes: role, department, clearance level, employment status
;;    - Resource attributes: owner, classification, sensitivity, department
;;    - Environmental context: time, location, device, IP address
;;    - Action: read, write, delete, admin, export
;;    This enables fine-grained policies like "managers can read reports from
;;    their department during business hours from office IP addresses."
;;
;; 2. Policy as Data
;;    Authorization policies are represented as data structures (maps, vectors)
;;    rather than code. This allows:
;;    - Storing policies in databases
;;    - Versioning policies over time
;;    - Dynamic policy updates without code deployment
;;    - Policy testing and simulation
;;    - Externalizing authorization logic from application code
;;
;; 3. First-Match Wins Strategy
;;    We evaluate rules in order and return on first match. This requires
;;    careful rule ordering: most specific rules first, general rules last.
;;    Example order: deny rules → specific allow rules → general allow rules.
;;    This is simpler than "all matching rules" but requires policy discipline.
;;
;; 4. Predicates for Complex Conditions
;;    Rules can include :conditions - a vector of predicate functions:
;;    [(fn [user resource ctx] (= (:dept user) (:dept resource)))]
;;    These are arbitrary boolean-returning functions that access all context.
;;    This provides unlimited flexibility for complex business rules.
;;
;; 5. Deny by Default (Secure Defaults)
;;    If no rules match, we deny access. This is the secure default -
;;    everything is forbidden unless explicitly allowed. The opposite
;;    (allow by default) would be a security vulnerability. Production
;;    authorization systems always deny by default for defense in depth.
;;
;; 6. Audit Trail with Matched Rules
;;    We return which rule matched (:matched-rules) for audit purposes.
;;    In production, this would be logged: "User U123 accessed DOC-456:
;;    action=delete, rule=R5, allowed=true, timestamp=...". This audit
;;    trail is critical for compliance (HIPAA, SOX, GDPR).
;;
;; 7. Separation of Policy and Enforcement
;;    The authorize function is pure - it evaluates policy but doesn't
;;    enforce it. Enforcement happens in controllers/middleware that call
;;    authorize and either allow requests through or return 403 Forbidden.
;;    This separation makes testing easier and enables policy caching.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/pure-functions/exemplo3.md
;;
;; Pattern used: Complex multi-branch evaluation with priority ordering
;;
;; The reference shows fraud/risk analysis with multiple condition checks:
;;   (cond
;;     (critical-fraud-signal? transaction) :block
;;     (high-risk-pattern? transaction) :manual-review
;;     (medium-risk? transaction) :additional-verification
;;     :else :approved)
;;
;; Real-world usage: Production systems use this pattern for:
;; - Authorization (access control to resources)
;; - Rate limiting (allow/deny based on usage patterns)
;; - Feature flags (enable features based on user/context attributes)
;; - Content moderation (approve/flag/reject based on rules)
;; - Pricing (apply discounts based on customer attributes)
;;
;; The key insight: Complex business logic benefits from explicit, auditable
;; rule evaluation. Representing rules as data enables runtime flexibility.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple role-based access
  (authorize
    {:rules [{:id "R1"
              :roles #{:admin}
              :actions #{:read :write :delete}
              :allow true}]}
    {:user-id "U123" :roles #{:admin}}
    {:id "DOC-456" :type :document}
    :delete
    {})
  ;; => {:allow true
  ;;     :reason "Rule R1: Access granted"
  ;;     :matched-rules ["R1"]
  ;;     :conditions []}

  ;; Example 2: Attribute-based with conditions
  (authorize
    {:rules [{:id "R2"
              :conditions [(fn [user resource ctx]
                             (and (= (:department user) (:owner-dept resource))
                                  (< (:hour ctx) 18)))]
              :actions #{:read :write}
              :allow true}]}
    {:user-id "U123" :department "engineering" :roles #{:developer}}
    {:id "DOC-789" :owner-dept "engineering"}
    :write
    {:hour 14})
  ;; => {:allow true
  ;;     :reason "Rule R2: Access granted"
  ;;     :matched-rules ["R2"]
  ;;     :conditions []}

  ;; Example 3: Denied - no matching rules
  (authorize
    {:rules [{:id "R3"
              :roles #{:admin}
              :actions #{:delete}
              :allow true}]}
    {:user-id "U456" :roles #{:user}}
    {:id "DOC-999"}
    :delete
    {})
  ;; => {:allow false
  ;;     :reason "No matching rules for action :delete"
  ;;     :matched-rules []
  ;;     :conditions []}

  ;; Example 4: Multiple rules - first match wins
  (authorize
    {:rules [{:id "R4-deny"
              :roles #{:intern}
              :actions #{:delete}
              :allow false}
             {:id "R5-allow"
              :actions #{:delete}
              :allow true}]}
    {:user-id "U789" :roles #{:intern}}
    {:id "DOC-111"}
    :delete
    {})
  ;; => {:allow false
  ;;     :reason "Rule R4-deny: Access denied"
  ;;     :matched-rules ["R4-deny"]
  ;;     :conditions []}

  ;; Example 5: Context-based (time restriction)
  (authorize
    {:rules [{:id "R6-business-hours"
              :conditions [(fn [user resource ctx]
                             (and (>= (:hour ctx) 9)
                                  (<= (:hour ctx) 17)))]
              :actions #{:access-sensitive}
              :allow true}]}
    {:user-id "U999" :roles #{:analyst}}
    {:id "REPORT-SENSITIVE"}
    :access-sensitive
    {:hour 20})
  ;; => {:allow false
  ;;     :reason "No matching rules for action :access-sensitive"
  ;;     :matched-rules []
  ;;     :conditions []}

  ;; Example 6: Resource ownership check
  (authorize
    {:rules [{:id "R7-owner"
              :conditions [(fn [user resource ctx]
                             (= (:user-id user) (:owner-id resource)))]
              :actions #{:read :write :delete}
              :allow true}]}
    {:user-id "U-OWNER" :roles #{:user}}
    {:id "POST-123" :owner-id "U-OWNER"}
    :delete
    {})
  ;; => {:allow true
  ;;     :reason "Rule R7-owner: Access granted"
  ;;     :matched-rules ["R7-owner"]
  ;;     :conditions []}

  ;; Example 7: Complex multi-condition rule
  (authorize
    {:rules [{:id "R8-complex"
              :roles #{:manager}
              :conditions [(fn [user resource ctx]
                             (= (:department user) (:department resource)))
                           (fn [user resource ctx]
                             (< (:sensitivity resource) 3))
                           (fn [user resource ctx]
                             (:vpn-connected ctx))]
              :actions #{:export}
              :allow true}]}
    {:user-id "U-MGR" :roles #{:manager} :department "sales"}
    {:id "DATA-EXPORT" :department "sales" :sensitivity 2}
    :export
    {:vpn-connected true})
  ;; => {:allow true
  ;;     :reason "Rule R8-complex: Access granted"
  ;;     :matched-rules ["R8-complex"]
  ;;     :conditions []}
)

;; TESTS
;; -----

(defn -test []
  ;; Test role-based access
  (let [result (authorize
                 {:rules [{:id "R1" :roles #{:admin} :actions #{:delete} :allow true}]}
                 {:user-id "U1" :roles #{:admin}}
                 {:id "DOC1"}
                 :delete
                 {})]
    (assert (:allow result) "Admin should be allowed to delete")
    (assert (= (:matched-rules result) ["R1"]) "Should match R1"))

  ;; Test denied access - no matching rule
  (let [result (authorize
                 {:rules [{:id "R2" :roles #{:admin} :actions #{:delete} :allow true}]}
                 {:user-id "U2" :roles #{:user}}
                 {:id "DOC2"}
                 :delete
                 {})]
    (assert (not (:allow result)) "User should be denied")
    (assert (empty? (:matched-rules result)) "Should have no matched rules"))

  ;; Test condition-based access
  (let [result (authorize
                 {:rules [{:id "R3"
                           :conditions [(fn [u r c] (= (:dept u) (:dept r)))]
                           :actions #{:read}
                           :allow true}]}
                 {:user-id "U3" :dept "eng" :roles #{:dev}}
                 {:id "DOC3" :dept "eng"}
                 :read
                 {})]
    (assert (:allow result) "Same department should allow access"))

  ;; Test condition failure
  (let [result (authorize
                 {:rules [{:id "R4"
                           :conditions [(fn [u r c] (< (:hour c) 18))]
                           :actions #{:access}
                           :allow true}]}
                 {:user-id "U4" :roles #{:user}}
                 {:id "RES4"}
                 :access
                 {:hour 20})]
    (assert (not (:allow result)) "After hours should deny"))

  ;; Test first-match wins
  (let [result (authorize
                 {:rules [{:id "R5-deny" :roles #{:intern} :actions #{:write} :allow false}
                          {:id "R6-allow" :actions #{:write} :allow true}]}
                 {:user-id "U5" :roles #{:intern}}
                 {:id "DOC5"}
                 :write
                 {})]
    (assert (not (:allow result)) "First matching rule should deny")
    (assert (= (:matched-rules result) ["R5-deny"]) "Should match deny rule"))

  (println "✓ All tests passed! The authorize function works correctly."))

;; Run: (-test)
