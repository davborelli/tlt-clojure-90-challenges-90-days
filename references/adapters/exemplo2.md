(ns aang.wire.out.avatar
  (:require [aang.models.avatar :as models.avatar]
            [common-core.types.time :as t-time]
            [schema.core :as s]))

(s/defschema CustomersAvatarResult
  {:customers-avatar-document [models.avatar/CustomerAvatar]})

(s/defschema CustomerAvatarResult
  {:customer-avatar-document models.avatar/CustomerAvatar})

(s/defschema AvatarDocument
  {:avatar models.avatar/Avatar})

(def ImageContent
  s/Str)

(s/defschema AvatarUploaded
  {:id           s/Uuid
   :customer-id  s/Uuid
   :content      ImageContent
   :image-source models.avatar/ImageSource
   :created-at   t-time/LocalDateTime})

(s/defschema AvatarImageURL
  {:id  s/Uuid
   :url (s/maybe s/Str)})

(s/defschema AvatarStored
  {:id           s/Uuid
   :customer-id  s/Uuid})