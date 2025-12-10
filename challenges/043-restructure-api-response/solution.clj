;; =============================================================================
;; 043 - RESTRUCTURE API RESPONSE
;; Level: 9/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; This adapter transforms complex API responses by extracting a nested
;; collection, mapping over it to flatten each item, and discarding metadata.
;; We use get-in for extraction and map for collection transformation.
;;
;; The approach separates concerns: extract the collection, then define a
;; transformation function for individual items. This makes the code testable
;; and the transformation logic clear. We go from deep nesting (3 levels) to
;; flat maps suitable for database storage or UI display.
;;
;; This pattern is ubiquitous in production systems that consume external APIs:
;; social media feeds, payment gateways, analytics services, etc. The external
;; format rarely matches internal needs, requiring systematic restructuring.

(ns challenge-043.solution)

;; IMPLEMENTATION
;; --------------

(defn transform-user
  "Transforms a single nested user into flat format.

  Parameters:
  - user: Nested user map from API

  Returns: Flat user map"
  [user]
  {:user-id (:id user)
   :name    (get-in user [:profile :name])
   :email   (get-in user [:profile :contact :email])
   :phone   (get-in user [:profile :contact :phone])})

(defn restructure-response
  "Restructures complex API response into flat user maps.

  Parameters:
  - api-response: Nested API response with metadata

  Returns: Vector of flattened user maps"
  [api-response]
  (let [users (get-in api-response [:data :users])]
    ;; Transform each user, result is already a vector from mapv
    (mapv transform-user users)))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Separation of Concerns: Collection vs Item
;;    We separate two transformations:
;;    - restructure-response handles collection-level extraction
;;    - transform-user handles individual item transformation
;;    This makes testing easier: we can test item transformation in isolation
;;    and collection extraction separately.
;;
;; 2. mapv for Vector Result
;;    map returns a lazy sequence, mapv returns a vector immediately.
;;    For adapters, vectors are often preferred because:
;;    - They're more common in domain models
;;    - They have better equality semantics for testing
;;    - They're strict (not lazy) which is safer for side-effectful contexts
;;
;; 3. Discarding Metadata
;;    API responses often include metadata (:status, :timestamp, :pagination)
;;    that's useful for the HTTP layer but irrelevant for business logic.
;;    Adapters are the right place to strip this metadata, keeping domain
;;    models clean and focused on business data.
;;
;; 4. Deep Nesting to Flat Structure
;;    External APIs often nest data for organization:
;;    - user → profile → name
;;    - user → profile → contact → email
;;    But internal systems prefer flat structures for:
;;    - Easier querying
;;    - Simpler validation
;;    - Better database mapping
;;    Adapters bridge this impedance mismatch.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Nested extraction + collection mapping
;;
;; Real-world usage: The reference code shows similar patterns:
;;   (let [users (get-in response [:data :users])]
;;     (map transform-user users))
;;
;; And individual item transformation:
;;   {:internal-field (get-in external [:nested :field])
;;    :another-field (get-in external [:deep :nested :field])}
;;
;; This demonstrates how production adapters handle complex API responses,
;; extracting collections and transforming each item to match internal domain
;; models. The pattern is essential for API integration, ETL pipelines, and
;; data normalization.

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: API response with two users
  (restructure-response
    {:status "success"
     :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 2}
     :data {:users [{:id 1
                     :profile {:name "John Doe"
                               :contact {:email "john@example.com"
                                         :phone "555-0100"}}}
                    {:id 2
                     :profile {:name "Jane Smith"
                               :contact {:email "jane@example.com"
                                         :phone "555-0200"}}}]}})
  ;; => [{:user-id 1, :name "John Doe", :email "john@example.com", :phone "555-0100"}
  ;;     {:user-id 2, :name "Jane Smith", :email "jane@example.com", :phone "555-0200"}]

  ;; Example 2: Single user response
  (restructure-response
    {:status "success"
     :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 1}
     :data {:users [{:id 999
                     :profile {:name "Alice Johnson"
                               :contact {:email "alice@example.com"
                                         :phone "555-9999"}}}]}})
  ;; => [{:user-id 999, :name "Alice Johnson", :email "alice@example.com", :phone "555-9999"}]

  ;; Example 3: Empty users list
  (restructure-response
    {:status "success"
     :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 0}
     :data {:users []}})
  ;; => []
)

;; TESTS
;; -----

(defn -test []
  (let [result (restructure-response
                 {:status "success"
                  :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 2}
                  :data {:users [{:id 1
                                  :profile {:name "John Doe"
                                            :contact {:email "john@example.com"
                                                      :phone "555-0100"}}}
                                 {:id 2
                                  :profile {:name "Jane Smith"
                                            :contact {:email "jane@example.com"
                                                      :phone "555-0200"}}}]}})]
    ;; Test result is a vector
    (assert (vector? result)
            "Result should be a vector")
    ;; Test correct number of users
    (assert (= (count result) 2)
            "Should have 2 users")
    ;; Test first user
    (assert (= (first result)
               {:user-id 1 :name "John Doe" :email "john@example.com" :phone "555-0100"})
            "First user should be correctly transformed")
    ;; Test second user
    (assert (= (second result)
               {:user-id 2 :name "Jane Smith" :email "jane@example.com" :phone "555-0200"})
            "Second user should be correctly transformed"))

  ;; Test single user
  (let [result (restructure-response
                 {:status "success"
                  :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 1}
                  :data {:users [{:id 999
                                  :profile {:name "Alice Johnson"
                                            :contact {:email "alice@example.com"
                                                      :phone "555-9999"}}}]}})]
    (assert (= (count result) 1)
            "Should handle single user")
    (assert (= (:user-id (first result)) 999)
            "Should extract correct user ID"))

  ;; Test empty users
  (let [result (restructure-response
                 {:status "success"
                  :metadata {:timestamp "2024-01-15T10:00:00" :page 1 :total 0}
                  :data {:users []}})]
    (assert (= result [])
            "Should handle empty users list"))

  (println "✓ All tests passed!"))

;; Run: (-test)
