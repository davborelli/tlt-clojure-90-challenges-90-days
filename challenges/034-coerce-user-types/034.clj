(ns coerce-user-types)

(defn coerce-user-types
  [{:keys [name age active role]}]
  {:name (str name)
   :age (parse-long age)
   :active (parse-boolean active)
   :role (keyword role)})

(defn- tst []
  (assert (=
(coerce-user-types {:name "John" :age "25" :active "true" :role "admin"})
{:name "John" :age 25 :active true :role :admin}))

(assert (=
(coerce-user-types {:name "Jane" :age "30" :active "false" :role "user"})
{:name "Jane" :age 30 :active false :role :user}))

(assert (=
(coerce-user-types {:name "Bob" :age "45" :active "true" :role "moderator"})
{:name "Bob" :age 45 :active true :role :moderator}))

  "SUCCESS")

(tst)
