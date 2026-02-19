(ns transform-nested-order)

(defn transform-order
  [order-data]
  )

(defn- tst []
  (assert (=
(transform-order 
  {:order-id "ORD-123" 
   :customer {:personal {:name "John Doe" :email "john@example.com"} 
              :shipping {:address {:street "123 Main St" :city "Springfield" :zip "12345"}}} 
   :total "99.50"})
{:id "ORD-123" 
 :customer-name "John Doe" 
 :customer-email "john@example.com" 
 :shipping-street "123 Main St" 
 :shipping-city "Springfield" 
 :shipping-zip "12345" 
 :total 99.5}))

  "SUCCESS")

(tst)
