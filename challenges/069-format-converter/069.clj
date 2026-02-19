(ns format-converter)

(defn convert-format
  [data from-format to-format]
  )

(defn- tst []
  (assert (=
(convert-format
  {:id "U123" :name "John Doe" :email_address "john@example.com" :is_active 1}
  :database
  :domain)
{:user-id "U123" :full-name "John Doe" :email "john@example.com" :active true}))

(assert (=
(convert-format
  {:user-id "U456" :full-name "Jane Smith" :email "jane@example.com" :active false}
  :domain
  :api)
{:userId "U456" :fullName "Jane Smith" :email "jane@example.com" :status "inactive"}))

(assert (=
(convert-format
  {:user-id "U789" :full-name "Bob Wilson" :email "bob@example.com" :active true}
  :domain
  :csv)
"U789,Bob Wilson,bob@example.com,true"))

  "SUCCESS")

(tst)
