;; =============================================================================
;; 083 - ADVANCED STATE MACHINE
;; Level: 17/18 | Type: Controller
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; State machines are fundamental to software systems: workflow engines (document
;; approval), protocol implementations (TCP, HTTP), game engines (character states),
;; and UI interactions (form wizard). Simple state machines just transition between
;; states. Advanced state machines add: guard conditions (transition only if
;; condition met), side effects (actions on transition), and history tracking.
;;
;; This implementation models state machines as pure data structures. Transitions
;; are validated against a definition, guards are predicates that must pass, and
;; effects are collected as data (not executed) maintaining purity. The history
;; provides an audit trail critical for debugging and compliance.
;;
;; The pattern separates policy (state machine definition) from mechanism
;; (transition engine), making it easy to modify workflows without changing code.
;; Production systems use state machines for order fulfillment, user onboarding,
;; payment processing, and regulatory compliance workflows where audit trails
;; are mandatory.

(ns challenge-083.solution)

;; IMPLEMENTATION
;; --------------

(defn- get-transition
  "Gets transition target state for given state and event"
  [definition current-state event]
  (get-in definition [:transitions current-state event]))

(defn- get-guard
  "Gets guard function for transition"
  [definition current-state event]
  (get-in definition [:guards current-state event]))

(defn- get-effects
  "Gets effects for transition"
  [definition current-state event]
  (get-in definition [:effects current-state event] []))

(defn- check-guard
  "Checks if guard condition passes"
  [guard context event-data]
  (if guard
    (try
      (guard context event-data)
      (catch Exception e
        false))
    true))  ; No guard means always pass

(defn- create-history-entry
  "Creates history entry for transition"
  [from-state to-state event context timestamp]
  {:from from-state
   :to to-state
   :event event
   :timestamp timestamp
   :context-snapshot (select-keys context [:version :key-fields])})

(defn- update-context
  "Updates context based on event data"
  [context event-data]
  (merge context event-data))

(defn process-event
  "Processes event in state machine, potentially transitioning state.

  Parameters:
  - machine-state: Current state machine state
  - event: Event triggering potential transition
  - event-data: Data associated with event

  Returns: New machine state or error"
  [machine-state event event-data]
  (let [{:keys [current-state context definition history]} machine-state
        target-state (get-transition definition current-state event)
        guard (get-guard definition current-state event)
        effects (get-effects definition current-state event)
        timestamp (System/currentTimeMillis)]

    (cond
      ;; No transition defined
      (nil? target-state)
      (assoc machine-state
        :error (format "No transition defined for event %s from state %s"
                      event current-state))

      ;; Guard condition failed
      (not (check-guard guard context event-data))
      (assoc machine-state
        :error (format "Guard condition failed for transition %s -> %s"
                      current-state target-state))

      ;; Valid transition
      :else
      (let [new-context (update-context context event-data)
            history-entry (create-history-entry current-state target-state
                                              event new-context timestamp)]
        (-> machine-state
            (assoc :current-state target-state)
            (assoc :context new-context)
            (update :history conj history-entry)
            (assoc :effects effects)
            (dissoc :error))))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. State Machine Fundamentals
;;    A state machine consists of: states (discrete conditions), transitions
;;    (allowed moves between states), events (triggers for transitions), and
;;    actions (side effects). Example: Document states (draft→review→published),
;;    transitions triggered by events (submit, approve, reject), actions (send
;;    notification, update index). State machines enforce valid paths through
;;    workflows, preventing invalid states (can't publish without review).
;;
;; 2. Guard Conditions
;;    Guards are predicates that determine if a transition is allowed. They check
;;    context and event data: "Can approve document if 2+ reviewers approved",
;;    "Can publish if no critical issues". Guards separate business rules from
;;    structure, making them testable. In production, guards might check database
;;    state, user permissions, or external service status. Failed guards leave
;;    state unchanged, unlike transitions which must complete.
;;
;; 3. Side Effects as Data
;;    Rather than executing side effects immediately (printing, database writes,
;;    API calls), we collect them as data: [:send-notification :update-index].
;;    This keeps the state machine pure, enabling testing without mocks. The
;;    caller executes effects after transition succeeds. This pattern (effect
;;    as data) is central to Clojure's functional approach and used by Re-frame,
;;    Pedestal, and other frameworks.
;;
;; 4. State History and Audit Trail
;;    Every transition is recorded with: from-state, to-state, event, timestamp,
;;    context snapshot. This provides complete audit trail for compliance (who
;;    approved when), debugging (how did we get to this state), and analytics
;;    (average time in review). Financial and healthcare systems require audit
;;    trails by law. History enables "time travel debugging" - replay to any point.
;;
;; 5. Context vs State
;;    State is discrete (draft, review, published). Context is continuous data
;;    that evolves (reviewer count, comments, metadata). Separating them clarifies
;;    design: state determines allowed transitions, context stores data. Context
;;    updates during transitions. In this implementation, context is merged with
;;    event-data, allowing transitions to update context values.
;;
;; 6. Declarative State Machine Definition
;;    The state machine is defined as data: {:transitions {:draft {:submit :review}}}
;;    rather than code. This enables: non-programmers to modify workflows, runtime
;;    workflow changes, workflow versioning, and workflow visualization tools. The
;;    definition is validated once at creation, then used for all transitions.
;;    Production systems store definitions in databases for dynamic workflow management.
;;
;; 7. Error Handling Strategies
;;    This implementation returns machine state with :error field rather than
;;    throwing exceptions. This allows caller to decide handling strategy: log
;;    and continue, retry, notify user. The state remains unchanged on error,
;;    maintaining consistency. Production systems might use Result/Either monads
;;    for more explicit error handling, but maps with :error key are simpler and
;;    idiomatic Clojure.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/controllers/exemplo2.md, exemplo5.md
;;
;; Pattern used: Multi-step workflow with state management and validation
;;
;; Real-world usage: The reference examples show controllers managing complex
;; workflows with state tracking. State machines formalize this pattern, making
;; workflows explicit and auditable. Financial services like Nubank use state
;; machines for transaction processing, loan applications, and customer onboarding
;; where each step must be validated and audited.
;;
;; The threading macro pattern (-> state (assoc ...) (update ...)) mirrors how
;; state transitions update multiple fields while maintaining immutability.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple transition
  (process-event
    {:current-state :draft
     :context {:doc-id "DOC-123" :version 1}
     :history []
     :definition {:transitions {:draft {:submit :review}}}}
    :submit
    {})
  ;; => {:current-state :review
  ;;     :context {:doc-id "DOC-123" :version 1}
  ;;     :history [{:from :draft :to :review :event :submit ...}]
  ;;     :effects []}

  ;; Example 2: Transition with guard (insufficient approvals)
  (process-event
    {:current-state :review
     :context {:doc-id "DOC-123" :approvals 1 :required-approvals 2}
     :history []
     :definition {:transitions {:review {:approve :approved}}
                  :guards {:review {:approve (fn [ctx _]
                                              (>= (:approvals ctx)
                                                  (:required-approvals ctx)))}}}}
    :approve
    {})
  ;; => {:current-state :review  ; Unchanged
  ;;     :error "Guard condition failed for transition :review -> :approved"
  ;;     ...}

  ;; Example 3: Transition with guard passing
  (process-event
    {:current-state :review
     :context {:doc-id "DOC-123" :approvals 2 :required-approvals 2}
     :history []
     :definition {:transitions {:review {:approve :approved}}
                  :guards {:review {:approve (fn [ctx _]
                                              (>= (:approvals ctx)
                                                  (:required-approvals ctx)))}}}}
    :approve
    {})
  ;; => {:current-state :approved
  ;;     :context {:doc-id "DOC-123" :approvals 2 :required-approvals 2}
  ;;     :effects []
  ;;     ...}

  ;; Example 4: Transition with side effects
  (process-event
    {:current-state :review
     :context {:doc-id "DOC-123"}
     :history []
     :definition {:transitions {:review {:publish :published}}
                  :effects {:review {:publish [:send-notification
                                              :update-search-index
                                              :invalidate-cache]}}}}
    :publish
    {})
  ;; => {:current-state :published
  ;;     :effects [:send-notification :update-search-index :invalidate-cache]
  ;;     ...}

  ;; Example 5: Undefined transition
  (process-event
    {:current-state :draft
     :context {}
     :history []
     :definition {:transitions {:draft {:submit :review}}}}
    :delete
    {})
  ;; => {:current-state :draft  ; Unchanged
  ;;     :error "No transition defined for event :delete from state :draft"
  ;;     ...}

  ;; Example 6: Context update during transition
  (process-event
    {:current-state :review
     :context {:doc-id "DOC-123" :reviewer-count 1}
     :history []
     :definition {:transitions {:review {:add-reviewer :review}}}}
    :add-reviewer
    {:reviewer-count 2 :new-reviewer "user-456"})
  ;; => {:current-state :review
  ;;     :context {:doc-id "DOC-123"
  ;;              :reviewer-count 2
  ;;              :new-reviewer "user-456"}
  ;;     ...}

  ;; Example 7: Multiple transitions building history
  (let [machine {:current-state :draft
                :context {:doc-id "DOC-123"}
                :history []
                :definition {:transitions {:draft {:submit :review}
                                          :review {:approve :approved}
                                          :approved {:publish :published}}}}
        after-submit (process-event machine :submit {})
        after-approve (process-event after-submit :approve {})
        after-publish (process-event after-approve :publish {})]
    (:history after-publish))
  ;; => [{:from :draft :to :review :event :submit ...}
  ;;     {:from :review :to :approved :event :approve ...}
  ;;     {:from :approved :to :published :event :publish ...}]
)

;; TESTS
;; -----

(defn -test []
  ;; Test simple transition
  (let [result (process-event
                 {:current-state :draft
                  :context {:doc-id "DOC-1"}
                  :history []
                  :definition {:transitions {:draft {:submit :review}}}}
                 :submit
                 {})]
    (assert (= (:current-state result) :review)
            "Should transition to review state")
    (assert (= (count (:history result)) 1)
            "Should add entry to history"))

  ;; Test guard failure
  (let [result (process-event
                 {:current-state :review
                  :context {:approvals 1 :required 2}
                  :history []
                  :definition {:transitions {:review {:approve :approved}}
                               :guards {:review {:approve (fn [ctx _]
                                                          (>= (:approvals ctx) (:required ctx)))}}}}
                 :approve
                 {})]
    (assert (contains? result :error)
            "Should return error when guard fails")
    (assert (= (:current-state result) :review)
            "Should not change state when guard fails"))

  ;; Test guard success
  (let [result (process-event
                 {:current-state :review
                  :context {:approvals 2 :required 2}
                  :history []
                  :definition {:transitions {:review {:approve :approved}}
                               :guards {:review {:approve (fn [ctx _]
                                                          (>= (:approvals ctx) (:required ctx)))}}}}
                 :approve
                 {})]
    (assert (= (:current-state result) :approved)
            "Should transition when guard passes"))

  ;; Test side effects collection
  (let [result (process-event
                 {:current-state :approved
                  :context {}
                  :history []
                  :definition {:transitions {:approved {:publish :published}}
                               :effects {:approved {:publish [:notify :index]}}}}
                 :publish
                 {})]
    (assert (= (:effects result) [:notify :index])
            "Should collect side effects"))

  ;; Test undefined transition
  (let [result (process-event
                 {:current-state :draft
                  :context {}
                  :history []
                  :definition {:transitions {:draft {:submit :review}}}}
                 :invalid-event
                 {})]
    (assert (contains? result :error)
            "Should error on undefined transition"))

  ;; Test context update
  (let [result (process-event
                 {:current-state :review
                  :context {:version 1}
                  :history []
                  :definition {:transitions {:review {:update :review}}}}
                 :update
                 {:version 2 :editor "user-123"})]
    (assert (= (:version (:context result)) 2)
            "Should update context with event data"))

  ;; Test history tracking
  (let [result (process-event
                 {:current-state :draft
                  :context {}
                  :history []
                  :definition {:transitions {:draft {:submit :review}}}}
                 :submit
                 {})]
    (assert (= (get-in result [:history 0 :from]) :draft)
            "History should track from-state")
    (assert (= (get-in result [:history 0 :to]) :review)
            "History should track to-state")
    (assert (= (get-in result [:history 0 :event]) :submit)
            "History should track event"))

  (println "✓ All tests passed! Advanced state machine works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
