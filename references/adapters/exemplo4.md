(ns among-us.adapters.capture-info
  (:require [among-us.models.capture-info :as models.capture-info]
            [among-us.wire.in.docs-capture :as in.docs-capture]
            [common-core.misc :as misc]
            [schema.core :as s]))

(s/defn wire->* :- models.capture-info/CaptureInfo
  [{docs-capture-id :id
    {:keys [customer-illiterate?]} :metadata
    :keys [completed-status capture-method flow validation-id]} :- in.docs-capture/DocsCapture]
  (misc/assoc-some {:docs-capture-id docs-capture-id
                    :completed-status completed-status
                    :metadata (misc/assoc-some {}
                                :customer-illiterate? customer-illiterate?)}
    :method (models.capture-info/capture-methods capture-method)
    :flow flow
    :validation-id validation-id))