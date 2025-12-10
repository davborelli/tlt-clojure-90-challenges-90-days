;; =============================================================================
;; 079 - NESTED DATA EXTRACTOR
;; Level: 16/18 | Type: Adapter
;; =============================================================================

;; SOLUTION EXPLANATION
;; --------------------
;; Modern APIs, especially GraphQL and REST APIs returning complex domain
;; objects, often produce deeply nested JSON structures. Frontend applications
;; and backend services need to extract specific fields from these structures
;; without writing brittle navigation code that crashes on missing keys.
;;
;; This solution implements a declarative path-based extraction system inspired
;; by libraries like JSONPath and XPath. Instead of writing imperative navigation
;; code with multiple nil checks, you specify what data you want (paths), how
;; you want it named (aliases), what defaults to use (defaults), and what's
;; mandatory (required). The extractor handles all the complexity.
;;
;; The implementation uses Clojure's get-in for safe nested navigation, which
;; returns nil for missing paths without throwing exceptions. It collects all
;; extraction errors rather than failing fast, providing complete feedback about
;; what data is missing. This approach is more user-friendly and easier to debug
;; than cascading null pointer exceptions.
;;
;; This pattern is essential in microservices architectures where services
;; consume data from multiple upstream APIs, in ETL pipelines processing various
;; data sources, and in frontend applications normalizing backend responses for
;; local state management.

(ns challenge-079.solution)

;; IMPLEMENTATION
;; --------------

(defn- extract-single-path
  "Safely extracts a value from nested data using a path.
  Returns tuple [path value found?] where found? indicates if path existed."
  [data path]
  (let [value (get-in data path ::not-found)]
    (if (= value ::not-found)
      [path nil false]
      [path value true])))

(defn- apply-alias
  "Applies alias to path, or uses last element of path as key if no alias."
  [path aliases]
  (or (get aliases path)
      (last path)))

(defn- collect-extraction-errors
  "Collects errors for required paths that weren't found."
  [extractions required-paths]
  (let [missing-required (->> extractions
                             (filter (fn [[path _ found?]]
                                      (and (contains? required-paths path)
                                           (not found?))))
                             (map (fn [[path _ _]]
                                   (str "Required path not found: " (pr-str path)))))]
    (when (seq missing-required)
      missing-required)))

(defn- build-result-map
  "Builds result map from extractions, applying aliases and defaults."
  [extractions aliases defaults]
  (reduce
    (fn [result [path value found?]]
      (let [key (apply-alias path aliases)]
        (cond
          found?
          (assoc result key value)

          (contains? defaults path)
          (assoc result key (get defaults path))

          :else
          result)))
    {}
    extractions))

(defn extract-nested-data
  "Extracts values from deeply nested data structure using path specifications.

  Parameters:
  - data: The nested data structure (maps, vectors, or mixed)
  - extraction-spec: Map with :paths, :aliases, :defaults, :required
    - :paths - Vector of paths (each path is vector of keys)
    - :aliases - Map from path to desired output key
    - :defaults - Map from path to default value if missing
    - :required - Set of paths that must exist

  Returns: Map with extracted values or :extraction-errors if validation fails"
  [data {:keys [paths aliases defaults required] :or {aliases {} defaults {} required #{}}}]
  ;; Extract all paths, tracking which were found
  (let [extractions (map #(extract-single-path data %) paths)
        errors (collect-extraction-errors extractions required)]

    (if (seq errors)
      ;; If required paths missing, return partial data with errors
      (assoc (build-result-map extractions aliases defaults)
             :extraction-errors errors)
      ;; Otherwise return successful extraction
      (build-result-map extractions aliases defaults))))

;; CONCEPT EXPLANATIONS
;; --------------------
;;
;; 1. Path-Based Navigation (get-in)
;;    Clojure's get-in function safely navigates nested structures using a vector
;;    of keys: (get-in {:a {:b {:c 1}}} [:a :b :c]) => 1. It returns nil (or a
;;    provided default) if any key in the path doesn't exist, avoiding null pointer
;;    exceptions. This is safer than chained map access which would throw on missing
;;    intermediate keys. The function works with both maps and vectors, using
;;    keywords/strings for maps and integers for vector indices.
;;
;; 2. Sentinel Values for Missing Data
;;    Using ::not-found as a sentinel value lets us distinguish between "path exists
;;    with nil value" and "path doesn't exist". If we used nil as the indicator,
;;    we couldn't handle fields that legitimately contain nil. The :: creates a
;;    namespaced keyword unique to this namespace, preventing collisions with user
;;    data. This technique is common in Clojure for disambiguation scenarios.
;;
;; 3. Declarative Data Extraction
;;    Instead of imperative code with if statements and nil checks, we declare what
;;    we want (paths), how to name it (aliases), and what's required. This separation
;;    of specification from implementation makes the code more maintainable and
;;    testable. The extraction logic is reusable across different data structures
;;    by just changing the spec. This is similar to SQL's SELECT clause or GraphQL
;;    queries - you declare what you want, not how to get it.
;;
;; 4. Error Collection vs Fail-Fast
;;    This implementation collects all errors before returning, rather than throwing
;;    an exception on the first missing required field. This provides better user
;;    experience - showing "you're missing fields A, B, and C" instead of "missing
;;    A" then "missing B" then "missing C" across three attempts. The trade-off is
;;    more complex error handling logic, but it's worth it for data validation and
;;    form processing scenarios.
;;
;; 5. Tuple Pattern for State Tracking
;;    The extract-single-path function returns [path value found?] - a tuple carrying
;;    multiple pieces of information. This avoids needing a map for every extraction,
;;    which would be heavier. Tuples are idiomatic Clojure for returning multiple
;;    related values from a function. The found? boolean lets downstream code
;;    distinguish between missing paths and paths with nil values.
;;
;; 6. Reduce for Aggregation
;;    The build-result-map function uses reduce to build up the result map from
;;    extractions. This is the Clojure idiom for transforming collections into
;;    aggregated results. Starting with an empty map {}, we assoc each extracted
;;    value with its alias as key. The cond handles the logic: found values take
;;    precedence, then defaults, then omit the key entirely. This is more elegant
;;    than a loop with mutation.
;;
;; 7. Vector-Based Paths for Flexibility
;;    Using vectors for paths ([:user :profile :name]) provides flexibility - paths
;;    can be manipulated as data, stored in configuration, and composed dynamically.
;;    This enables meta-programming: code that generates extraction specs. Contrast
;;    with string-based paths ("user.profile.name") which require parsing, or
;;    function composition which can't be introspected or serialized. Vector paths
;;    are the sweet spot: structured but manipulable.

;; REFERENCE PATTERN
;; -----------------
;; This challenge is inspired by: references/adapters/exemplo1.md, exemplo2.md
;;
;; Pattern used: Deep nested structure transformation and extraction
;;
;; Real-world usage: The reference examples show adapters that transform nested
;; API responses into flat domain objects. This challenge extends that pattern
;; with declarative specifications, making it more flexible and reusable.
;;
;; Production systems use this pattern extensively when:
;; - Consuming GraphQL APIs that return deeply nested objects
;; - Processing configuration files (YAML, JSON) with hierarchical structure
;; - Extracting data from document databases (MongoDB, DynamoDB)
;; - Normalizing state in Redux/Re-frame from API responses
;; - Building data pipelines that transform between different schemas

;; USAGE EXAMPLES
;; --------------

(comment
  ;; Example 1: Simple nested extraction with aliases
  (extract-nested-data
    {:user {:profile {:name "John Doe"
                      :contact {:email "john@example.com"
                               :phone "555-1234"}}
            :settings {:theme "dark"}}}
    {:paths [[:user :profile :name]
             [:user :profile :contact :email]
             [:user :settings :theme]]
     :aliases {[:user :profile :name] :name
               [:user :profile :contact :email] :email
               [:user :settings :theme] :theme}})
  ;; => {:name "John Doe"
  ;;     :email "john@example.com"
  ;;     :theme "dark"}

  ;; Example 2: Using defaults for missing optional fields
  (extract-nested-data
    {:user {:profile {:name "Jane Smith"}}}
    {:paths [[:user :profile :name]
             [:user :profile :contact :email]
             [:user :settings :notifications]]
     :aliases {[:user :profile :name] :name
               [:user :profile :contact :email] :email
               [:user :settings :notifications] :notifications}
     :defaults {[:user :profile :contact :email] "no-email@example.com"
                [:user :settings :notifications] true}})
  ;; => {:name "Jane Smith"
  ;;     :email "no-email@example.com"
  ;;     :notifications true}

  ;; Example 3: Required paths validation with errors
  (extract-nested-data
    {:user {:profile {:name "Bob Jones"}}}
    {:paths [[:user :profile :name]
             [:user :profile :id]
             [:user :settings :role]]
     :required #{[:user :profile :id] [:user :settings :role]}
     :aliases {[:user :profile :name] :name
               [:user :profile :id] :user-id
               [:user :settings :role] :role}})
  ;; => {:name "Bob Jones"
  ;;     :extraction-errors ["Required path not found: [:user :profile :id]"
  ;;                        "Required path not found: [:user :settings :role]"]}

  ;; Example 4: Array/vector navigation by index
  (extract-nested-data
    {:orders [{:id 1 :total 100}
              {:id 2 :total 200}
              {:id 3 :total 300}]
     :user {:name "Alice"}}
    {:paths [[:orders 0 :id]
             [:orders 1 :total]
             [:orders 2 :id]
             [:user :name]]
     :aliases {[:orders 0 :id] :first-order-id
               [:orders 1 :total] :second-order-total
               [:orders 2 :id] :third-order-id
               [:user :name] :customer-name}})
  ;; => {:first-order-id 1
  ;;     :second-order-total 200
  ;;     :third-order-id 3
  ;;     :customer-name "Alice"}

  ;; Example 5: Complex nested structure (e-commerce order)
  (extract-nested-data
    {:order {:id "ORD-123"
             :customer {:id "CUST-456"
                       :name "Carol White"
                       :address {:street "123 Main St"
                                :city "Springfield"
                                :zip "12345"}}
             :items [{:product {:id "PROD-1" :name "Widget"}
                     :quantity 2
                     :price 29.99}
                    {:product {:id "PROD-2" :name "Gadget"}
                     :quantity 1
                     :price 49.99}]
             :payment {:method "credit-card"
                      :last4 "4242"}}}
    {:paths [[:order :id]
             [:order :customer :name]
             [:order :customer :address :city]
             [:order :items 0 :product :name]
             [:order :items 1 :price]
             [:order :payment :method]]
     :aliases {[:order :id] :order-id
               [:order :customer :name] :customer
               [:order :customer :address :city] :city
               [:order :items 0 :product :name] :first-item
               [:order :items 1 :price] :second-item-price
               [:order :payment :method] :payment-method}})
  ;; => {:order-id "ORD-123"
  ;;     :customer "Carol White"
  ;;     :city "Springfield"
  ;;     :first-item "Widget"
  ;;     :second-item-price 49.99
  ;;     :payment-method "credit-card"}

  ;; Example 6: Handling nil values vs missing keys
  (extract-nested-data
    {:user {:profile {:name "Dave"
                     :bio nil}}}
    {:paths [[:user :profile :name]
             [:user :profile :bio]
             [:user :profile :avatar]]
     :aliases {[:user :profile :name] :name
               [:user :profile :bio] :bio
               [:user :profile :avatar] :avatar}
     :defaults {[:user :profile :avatar] "default.png"}})
  ;; => {:name "Dave"
  ;;     :bio nil              ; nil is preserved (key exists)
  ;;     :avatar "default.png"} ; default used (key missing)

  ;; Example 7: All required fields present
  (extract-nested-data
    {:api-response {:status 200
                    :data {:user {:id "U123"
                                 :email "user@example.com"}}}}
    {:paths [[:api-response :status]
             [:api-response :data :user :id]
             [:api-response :data :user :email]]
     :required #{[:api-response :data :user :id]
                 [:api-response :data :user :email]}
     :aliases {[:api-response :status] :status
               [:api-response :data :user :id] :user-id
               [:api-response :data :user :email] :email}})
  ;; => {:status 200
  ;;     :user-id "U123"
  ;;     :email "user@example.com"}
)

;; TESTS
;; -----

(defn -test []
  ;; Test simple extraction with aliases
  (let [result (extract-nested-data
                 {:user {:profile {:name "John Doe"}}}
                 {:paths [[:user :profile :name]]
                  :aliases {[:user :profile :name] :name}})]
    (assert (= (:name result) "John Doe")
            "Should extract and alias simple nested value"))

  ;; Test defaults for missing paths
  (let [result (extract-nested-data
                 {:user {:profile {:name "Jane"}}}
                 {:paths [[:user :profile :name]
                          [:user :profile :email]]
                  :aliases {[:user :profile :name] :name
                            [:user :profile :email] :email}
                  :defaults {[:user :profile :email] "no-email@example.com"}})]
    (assert (= (:email result) "no-email@example.com")
            "Should use default for missing optional field"))

  ;; Test required path validation
  (let [result (extract-nested-data
                 {:user {:profile {:name "Bob"}}}
                 {:paths [[:user :profile :id]]
                  :required #{[:user :profile :id]}
                  :aliases {[:user :profile :id] :user-id}})]
    (assert (contains? result :extraction-errors)
            "Should return errors for missing required paths")
    (assert (some #(re-find #"Required path not found" %)
                  (:extraction-errors result))
            "Should include descriptive error message"))

  ;; Test array navigation
  (let [result (extract-nested-data
                 {:items [{:id 1} {:id 2}]}
                 {:paths [[:items 0 :id] [:items 1 :id]]
                  :aliases {[:items 0 :id] :first
                            [:items 1 :id] :second}})]
    (assert (= (:first result) 1)
            "Should extract from array by index")
    (assert (= (:second result) 2)
            "Should extract multiple array elements"))

  ;; Test nil value preservation
  (let [result (extract-nested-data
                 {:user {:bio nil}}
                 {:paths [[:user :bio]]
                  :aliases {[:user :bio] :bio}})]
    (assert (contains? result :bio)
            "Should include key even if value is nil")
    (assert (nil? (:bio result))
            "Should preserve nil value"))

  ;; Test complex nested structure
  (let [result (extract-nested-data
                 {:order {:customer {:address {:city "Springfield"}}}}
                 {:paths [[:order :customer :address :city]]
                  :aliases {[:order :customer :address :city] :city}})]
    (assert (= (:city result) "Springfield")
            "Should handle deeply nested paths"))

  ;; Test all required present
  (let [result (extract-nested-data
                 {:user {:id "U123" :email "user@example.com"}}
                 {:paths [[:user :id] [:user :email]]
                  :required #{[:user :id] [:user :email]}
                  :aliases {[:user :id] :id
                            [:user :email] :email}})]
    (assert (not (contains? result :extraction-errors))
            "Should not return errors when all required present")
    (assert (= (:id result) "U123")
            "Should extract all required fields"))

  (println "✓ All tests passed! Nested data extractor works correctly."))

;; Run the tests
;; Execute in REPL: (-test)
