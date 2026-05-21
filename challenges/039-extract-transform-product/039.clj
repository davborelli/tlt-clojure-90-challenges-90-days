(ns extract-transform-product)

(defn- round2
  [n]
  (/ (Math/round (* n 100.0)) 100.0))

(defn transform-product
  [product-data]
  (let [id               (get-in product-data [:product-id])
        name             (get-in product-data [:details :name])
        category         (get-in product-data [:details :category])
        price            (Double/parseDouble (get-in product-data [:pricing :base-price]))
        discount-percent (Double/parseDouble (get-in product-data [:pricing :discount-percent]))
        discounted-price (round2 (* price (- 1 (/ discount-percent 100))))
        stock            (Integer/parseInt (get-in product-data [:inventory :stock]))
        warehouse        (get-in product-data [:inventory :warehouse])]
    {:id               id
     :name             name
     :category         category
     :price            price
     :discounted-price discounted-price
     :stock            stock
     :warehouse        warehouse}))

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
