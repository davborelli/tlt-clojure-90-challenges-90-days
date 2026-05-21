(ns transform-nested-order)

(defn transform-order
  [order-data]
  (let [id              (get-in order-data [:order-id])
        customer-name   (get-in order-data [:customer :personal :name])
        customer-email  (get-in order-data [:customer :personal :email])
        customer-street (get-in order-data [:customer :shipping :address :street])
        customer-city   (get-in order-data [:customer :shipping :address :city])
        shipping-zip    (get-in order-data [:customer :shipping :address :zip])
        total           (get-in order-data [:total])]
    {:id id
     :customer-name customer-name
     :customer-email customer-email
     :shipping-street customer-street
     :shipping-city customer-city
     :shipping-zip shipping-zip
     :total (Double/parseDouble total)}))

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
