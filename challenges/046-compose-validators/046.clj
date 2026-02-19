(ns compose-validators)

(defn compose-validators
  [validators data]
  )

(defn- tst []
  (let [validate-required-name (fn [data] (when (empty? (:name data)) {:error "Name is required"}))
        validate-age-limit (fn [data] (when (< (:age data) 18) {:error "Must be 18 or older"}))
        validate-email (fn [data] (when-not (.contains (:email data) "@") {:error "Invalid email"}))]

    (assert (=
             (compose-validators [validate-required-name validate-age-limit validate-email] 
                                 {:name "John" :age 25 :email "john@example.com"})
             nil))

    (assert (=
             (compose-validators [validate-required-name validate-age-limit validate-email] 
                                 {:name "" :age 25 :email "john@example.com"})
             {:error "Name is required"}))

    (assert (=
             (compose-validators [validate-required-name validate-age-limit validate-email] 
                                 {:name "John" :age 16 :email "john@example.com"})
             {:error "Must be 18 or older"})))

  "SUCCESS")

(tst)
