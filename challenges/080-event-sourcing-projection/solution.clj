;; =============================================================================
;; 080 - EVENT SOURCING PROJECTION
;; Level: 16/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Event sourcing is a powerful architectural pattern used in financial systems,
;; collaboration tools (like Git), and e-commerce platforms. Instead of storing
;; just the current state (e.g., "balance: $500"), we store all state changes
;; as events (e.g., "deposit $1000", "withdraw $200", "withdraw $300"). The
;; current state is computed by replaying these events.
;;
;; This provides several critical benefits: complete audit trail (required for
;; financial compliance), ability to reconstruct state at any point in time
;; (time travel for debugging), event replay for bug fixes, and natural support
;; for event-driven architectures and CQRS (Command Query Responsibility Segregation).
;;
;; The implementation uses a reduce-based fold pattern - we start with initial
;; state and apply each event as a transformation. Multi-methods dispatch on
;; event type, making it easy to add new event types. The system handles business
;; rules (can't withdraw from locked account, can't overdraft) and maintains
;; audit information (version, rejected events).
;;
;; This pattern is production-grade: banks use it for transaction history, Git
;; uses it for version control, and event-driven microservices use it for
;; eventual consistency across distributed systems.

(ns challenge-080.solution)

;; IMPLEMENTATION
;; --------------

;; Multi-method for event-specific state transformations
(defmulti apply-event
  "Applies a single event to current state, returning new state"
  (fn [_state event] (:event-type event)))

;; Account created event
(defmethod apply-event :account-created
  [state {:keys [timestamp data]}]
  (merge state
         {:account-id (:account-id data)
          :owner (:owner data)
          :balance (:initial-balance data)
          :status :active
          :locked-reason nil
          :rejected-events []
          :last-updated timestamp}))

;; Deposit event
(defmethod apply-event :deposit-made
  [state {:keys [timestamp data]}]
  (if (= (:status state) :locked)
    ;; Reject deposit on locked account
    (-> state
        (update :rejected-events conj {:event-type :deposit-made
                                       :reason "Account locked"})
        (assoc :last-updated timestamp))
    ;; Process deposit
    (-> state
        (update :balance + (:amount data))
        (assoc :last-updated timestamp))))

;; Withdrawal event
(defmethod apply-event :withdrawal-made
  [state {:keys [timestamp data]}]
  (let [amount (:amount data)]
    (cond
      ;; Account is locked
      (= (:status state) :locked)
      (-> state
          (update :rejected-events conj {:event-type :withdrawal-made
                                         :reason "Account locked"})
          (assoc :last-updated timestamp))

      ;; Insufficient funds
      (< (:balance state) amount)
      (-> state
          (update :rejected-events conj {:event-type :withdrawal-made
                                         :reason "Insufficient funds"})
          (assoc :last-updated timestamp))

      ;; Process withdrawal
      :else
      (-> state
          (update :balance - amount)
          (assoc :last-updated timestamp)))))

;; Account locked event
(defmethod apply-event :account-locked
  [state {:keys [timestamp data]}]
  (-> state
      (assoc :status :locked)
      (assoc :locked-reason (:reason data))
      (assoc :last-updated timestamp)))

;; Account unlocked event
(defmethod apply-event :account-unlocked
  [state {:keys [timestamp]}]
  (-> state
      (assoc :status :active)
      (assoc :locked-reason nil)
      (assoc :last-updated timestamp)))

;; Interest accrued event
(defmethod apply-event :interest-accrued
  [state {:keys [timestamp data]}]
  (if (= (:status state) :locked)
    ;; Don't accrue interest on locked accounts
    (-> state
        (update :rejected-events conj {:event-type :interest-accrued
                                       :reason "Account locked"})
        (assoc :last-updated timestamp))
    ;; Calculate and add interest
    (let [rate (:rate data)
          interest (* (:balance state) rate)]
      (-> state
          (update :balance + interest)
          (assoc :last-updated timestamp)))))

;; Default handler for unknown events
(defmethod apply-event :default
  [state {:keys [event-type timestamp]}]
  (-> state
      (update :rejected-events conj {:event-type event-type
                                     :reason "Unknown event type"})
      (assoc :last-updated timestamp)))

(defn project-events
  "Projects final state by replaying sequence of events.

  Parameters:
  - events: Vector of event maps with :event-type, :timestamp, :data
  - initial-state: Starting state (defaults to empty map)

  Returns: Final state with :version and :last-updated metadata"
  ([events] (project-events events {}))
  ([events initial-state]
   (let [final-state (reduce
                       (fn [state event]
                         (apply-event state event))
                       initial-state
                       events)]
     ;; Add version (count of events processed)
     (assoc final-state :version (count events)))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Event Sourcing Fundamentals
;;    Event sourcing stores state changes as immutable events rather than current
;;    state. Traditional: "balance = 500". Event sourcing: ["created with $1000",
;;    "withdrew $300", "withdrew $200"] = 500. Benefits include: complete audit
;;    trail (who did what when), time travel (replay to any point), bug recovery
;;    (fix projection logic and replay), and event-driven architecture support.
;;    Financial systems require this for compliance and auditing.
;;
;; 2. Projection Pattern (Event Replay)
;;    A projection rebuilds current state by replaying events sequentially. This
;;    is a fold/reduce operation: start with initial state, apply each event as
;;    a transformation. The projection is deterministic - same events always
;;    produce same state. Multiple projections can exist (one for balance, one
;;    for audit log, one for analytics). If projection logic has bugs, fix it
;;    and replay all events to correct state.
;;
;; 3. Multi-methods for Event Dispatch
;;    Using defmulti/defmethod lets each event type have its own handler. The
;;    dispatch function (:event-type) selects which method to call. This is more
;;    maintainable than a giant case statement - each event handler is isolated,
;;    testable, and can be added without modifying existing code. It's the Open/
;;    Closed Principle: open for extension (new events), closed for modification.
;;
;; 4. State Transitions with Threading Macros
;;    The -> threading macro makes state transformations readable: start with
;;    current state, update balance, set timestamp. This is cleaner than nested
;;    function calls: (assoc (update state :balance + amount) :last-updated ts).
;;    Threading macros are idiomatic Clojure for sequential transformations on
;;    data structures. They make the "data pipeline" visible.
;;
;; 5. Business Rules in Event Handlers
;;    Each event handler enforces business rules: can't withdraw from locked
;;    account, can't overdraft, interest only accrues on active accounts. These
;;    rules are encoded in the projection logic, not the events themselves.
;;    Events are facts ("withdrawal attempted"), projections interpret them
;;    ("withdrawal rejected - account locked"). This separation is key to
;;    event sourcing flexibility.
;;
;; 6. Rejected Events Tracking
;;    Rather than silently ignoring invalid operations, we track rejected events
;;    in the state. This provides audit trail of attempted operations and why
;;    they failed. In production, rejected events might trigger alerts (fraud
;;    detection: multiple failed withdrawals), compliance reports (attempted
;;    access to frozen accounts), or analytics (feature usage patterns).
;;
;; 7. Immutability and Version Tracking
;;    Each event application creates new state rather than mutating existing
;;    state. The :version field counts events processed, enabling optimistic
;;    locking in concurrent systems. If two processes project same events, they
;;    get identical state. This determinism is crucial for distributed systems,
;;    testing, and debugging. Version numbers also enable snapshot optimization:
;;    store state at version 1000, replay from there instead of event 0.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo2.md, exemplo5.md
;;
;; Pattern used: Multi-step transactional flow with state management
;;
;; Real-world usage: The reference examples show controllers orchestrating
;; multiple operations with state tracking, similar to how event projections
;; maintain state through event sequences. Financial systems like Nubank use
;; event sourcing for transaction processing, ensuring every state change is
;; auditable and recoverable.
;;
;; The threading macro pattern (-> state (update ...) (assoc ...)) mirrors the
;; reference's approach to building up state through sequential operations,
;; particularly in exemplo5.md's transactional flows.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple account lifecycle
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-123"
             :owner "John Doe"
             :initial-balance 0}}
     {:event-type :deposit-made
      :timestamp 1100
      :data {:amount 1000}}
     {:event-type :withdrawal-made
      :timestamp 1200
      :data {:amount 200}}]
    {})
  ;; => {:account-id "ACC-123"
  ;;     :owner "John Doe"
  ;;     :balance 800
  ;;     :status :active
  ;;     :locked-reason nil
  ;;     :rejected-events []
  ;;     :version 3
  ;;     :last-updated 1200}

  ;; Example 2: Locked account with rejected operations
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-456" :owner "Jane Smith" :initial-balance 500}}
     {:event-type :account-locked
      :timestamp 1100
      :data {:reason "suspicious-activity"}}
     {:event-type :withdrawal-made
      :timestamp 1200
      :data {:amount 100}}
     {:event-type :deposit-made
      :timestamp 1250
      :data {:amount 200}}
     {:event-type :account-unlocked
      :timestamp 1300
      :data {}}]
    {})
  ;; => {:account-id "ACC-456"
  ;;     :owner "Jane Smith"
  ;;     :balance 500
  ;;     :status :active
  ;;     :locked-reason nil
  ;;     :version 5
  ;;     :last-updated 1300
  ;;     :rejected-events [{:event-type :withdrawal-made :reason "Account locked"}
  ;;                      {:event-type :deposit-made :reason "Account locked"}]}

  ;; Example 3: Interest accrual
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-789" :owner "Bob" :initial-balance 1000}}
     {:event-type :interest-accrued
      :timestamp 2000
      :data {:rate 0.05}}
     {:event-type :interest-accrued
      :timestamp 3000
      :data {:rate 0.05}}]
    {})
  ;; => {:account-id "ACC-789"
  ;;     :owner "Bob"
  ;;     :balance 1102.5 ; 1000 * 1.05 * 1.05
  ;;     :status :active
  ;;     :version 3
  ;;     :last-updated 3000}

  ;; Example 4: Insufficient funds rejection
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-111" :owner "Alice" :initial-balance 100}}
     {:event-type :withdrawal-made
      :timestamp 1100
      :data {:amount 150}}]
    {})
  ;; => {:account-id "ACC-111"
  ;;     :owner "Alice"
  ;;     :balance 100
  ;;     :status :active
  ;;     :version 2
  ;;     :last-updated 1100
  ;;     :rejected-events [{:event-type :withdrawal-made
  ;;                       :reason "Insufficient funds"}]}

  ;; Example 5: Multiple deposits and withdrawals
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-222" :owner "Charlie" :initial-balance 500}}
     {:event-type :deposit-made
      :timestamp 1100
      :data {:amount 300}}
     {:event-type :withdrawal-made
      :timestamp 1200
      :data {:amount 200}}
     {:event-type :deposit-made
      :timestamp 1300
      :data {:amount 150}}
     {:event-type :withdrawal-made
      :timestamp 1400
      :data {:amount 400}}]
    {})
  ;; => {:account-id "ACC-222"
  ;;     :owner "Charlie"
  ;;     :balance 350 ; 500 + 300 - 200 + 150 - 400
  ;;     :status :active
  ;;     :version 5
  ;;     :last-updated 1400}

  ;; Example 6: Unknown event type handling
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-333" :owner "Dave" :initial-balance 200}}
     {:event-type :unknown-operation
      :timestamp 1100
      :data {:something "invalid"}}]
    {})
  ;; => {:account-id "ACC-333"
  ;;     :owner "Dave"
  ;;     :balance 200
  ;;     :status :active
  ;;     :version 2
  ;;     :last-updated 1100
  ;;     :rejected-events [{:event-type :unknown-operation
  ;;                       :reason "Unknown event type"}]}

  ;; Example 7: Interest on locked account (rejected)
  (project-events
    [{:event-type :account-created
      :timestamp 1000
      :data {:account-id "ACC-444" :owner "Eve" :initial-balance 1000}}
     {:event-type :account-locked
      :timestamp 1100
      :data {:reason "fraud-investigation"}}
     {:event-type :interest-accrued
      :timestamp 2000
      :data {:rate 0.05}}]
    {})
  ;; => {:account-id "ACC-444"
  ;;     :owner "Eve"
  ;;     :balance 1000 ; No interest on locked account
  ;;     :status :locked
  ;;     :locked-reason "fraud-investigation"
  ;;     :version 3
  ;;     :last-updated 2000
  ;;     :rejected-events [{:event-type :interest-accrued
  ;;                       :reason "Account locked"}]}
)

;; TESTS
;; -----

(defn -test []
  ;; Test account creation and basic deposit
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-123" :owner "John" :initial-balance 0}}
                  {:event-type :deposit-made
                   :timestamp 1100
                   :data {:amount 1000}}])]
    (assert (= (:balance result) 1000)
            "Should process deposit correctly")
    (assert (= (:version result) 2)
            "Should track event version"))

  ;; Test withdrawal with sufficient funds
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-456" :owner "Jane" :initial-balance 500}}
                  {:event-type :withdrawal-made
                   :timestamp 1100
                   :data {:amount 200}}])]
    (assert (= (:balance result) 300)
            "Should process withdrawal correctly"))

  ;; Test withdrawal with insufficient funds
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-789" :owner "Bob" :initial-balance 100}}
                  {:event-type :withdrawal-made
                   :timestamp 1100
                   :data {:amount 150}}])]
    (assert (= (:balance result) 100)
            "Should not process withdrawal with insufficient funds")
    (assert (seq (:rejected-events result))
            "Should record rejected event"))

  ;; Test locked account
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-111" :owner "Alice" :initial-balance 500}}
                  {:event-type :account-locked
                   :timestamp 1100
                   :data {:reason "suspicious"}}
                  {:event-type :withdrawal-made
                   :timestamp 1200
                   :data {:amount 100}}])]
    (assert (= (:status result) :locked)
            "Should lock account")
    (assert (= (:balance result) 500)
            "Should not process operations on locked account")
    (assert (some #(= (:event-type %) :withdrawal-made) (:rejected-events result))
            "Should record rejected withdrawal"))

  ;; Test account unlock
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-222" :owner "Charlie" :initial-balance 300}}
                  {:event-type :account-locked
                   :timestamp 1100
                   :data {:reason "test"}}
                  {:event-type :account-unlocked
                   :timestamp 1200
                   :data {}}])]
    (assert (= (:status result) :active)
            "Should unlock account")
    (assert (nil? (:locked-reason result))
            "Should clear lock reason"))

  ;; Test interest accrual
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-333" :owner "Dave" :initial-balance 1000}}
                  {:event-type :interest-accrued
                   :timestamp 2000
                   :data {:rate 0.05}}])]
    (assert (= (:balance result) 1050.0)
            "Should accrue interest correctly"))

  ;; Test unknown event type
  (let [result (project-events
                 [{:event-type :account-created
                   :timestamp 1000
                   :data {:account-id "ACC-444" :owner "Eve" :initial-balance 200}}
                  {:event-type :invalid-event
                   :timestamp 1100
                   :data {}}])]
    (assert (some #(= (:reason %) "Unknown event type") (:rejected-events result))
            "Should handle unknown event types"))

  (println "✓ All tests passed! Event sourcing projection works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
