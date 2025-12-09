(ns aang.wire.in.avatar
  (:require [aang.models.avatar :as models.avatar]
            [common-core.types.time :as t-time]
            [schema.core :as s]))

(s/defschema CustomersPayload
  {:requester-id s/Uuid
   :customer-ids [s/Uuid]
   s/Keyword     s/Any})

(def AvatarUploadInput
  {:content      s/Str
   :image-source models.avatar/ImageSource})

(def ImageContent
  s/Str)

(s/defschema AvatarUploaded
  {:id           s/Uuid
   :customer-id  s/Uuid
   :content      ImageContent
   :image-source models.avatar/ImageSource
   :created-at   t-time/LocalDateTime
   s/Keyword     s/Any})