(ns extract-transform-product)

(defn transform-product
  [product-data]
  )

(defn- tst []
  (assert (=
(transform-product 
  {:product-id "PROD-001" 
   :details {:name "Laptop" :category "Electronics"} 
   :pricing {:base-price "999.99" :discount-percent "10"} 
   :inventory {:stock "50" :warehouse "NYC"}})
{:id "PROD-001" 
 :name "Laptop" 
 :category "Electronics" 
 :price 999.99 
 :discounted-price 899.99 
 :stock 50 
 :warehouse "NYC"}))

(assert (=
(transform-product 
  {:product-id "PROD-002" 
   :details {:name "Mouse" :category "Accessories"} 
   :pricing {:base-price "29.99" :discount-percent "20"} 
   :inventory {:stock "200" :warehouse "LA"}})
{:id "PROD-002" 
 :name "Mouse" 
 :category "Accessories" 
 :price 29.99 
 :discounted-price 23.99 
 :stock 200 
 :warehouse "LA"}))

  "SUCCESS")

(tst)
