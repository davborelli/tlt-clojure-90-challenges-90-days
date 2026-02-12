(ns order-fulfillment)

(defn fulfill-order
  [order-request]
  )

(defn- tst []
  (assert (=
(fulfill-order
  {:order-id "ORD-001"
   :customer-id "CUST-123"
   :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
           {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
   :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}})
{:order-id "ORD-001"
     :customer-id "CUST-123"
     :items [{:product-id "PROD-A" :quantity 2 :unit-price 25.00}
             {:product-id "PROD-B" :quantity 1 :unit-price 60.00}]
     :shipping-address {:street "123 Main St" :city "NYC" :zip "10001"}
     :validated true
     :inventory-reserved true
     :subtotal 110.0
     :shipping-cost 0.0
     :total 110.0
     :invoice-id "INV-ORD-001"
     :status :fulfilled}))

(assert (=
(fulfill-order
  {:order-id "ORD-002"
   :customer-id "CUST-456"
   :items []
   :shipping-address {:street "456 Oak Ave"}})
{:status :error :message "Order must have items"}))

  "SUCCESS")

(tst)
