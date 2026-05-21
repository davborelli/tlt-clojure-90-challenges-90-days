(ns api-to-internal 
  (:require
    [clojure.string :as str]))

(defn api->internal
  [api-response]
  (let [{product-id      :productId
         {name           :productName
          category       :category
          {price         :basePrice
           currency      :currency} :pricing} :productDetails
         {stock          :stockLevel
          {warehouse-id  :id
           warehouse-loc :location} :warehouse} :inventory} api-response  ]
    {:product-id product-id
     :name name
     :category (keyword category)
     :price price
     :currency (keyword (str/lower-case currency))
     :stock stock
     :warehouse-id warehouse-id
     :warehouse-location warehouse-loc}))

(defn- tst []
  (assert (=
(api->internal
  {:productId "PROD-123"
   :productDetails {:productName "Laptop"
                    :category "electronics"
                    :pricing {:basePrice 999.99 :currency "USD"}}
   :inventory {:stockLevel 50
               :warehouse {:id "WH-001" :location "New York"}}
   :metadata {:timestamp "2024-01-15" :version 2}})
{:product-id "PROD-123"
     :name "Laptop"
     :category :electronics
     :price 999.99
     :currency :usd
     :stock 50
     :warehouse-id "WH-001"
     :warehouse-location "New York"}))

  "SUCCESS")

(tst)
