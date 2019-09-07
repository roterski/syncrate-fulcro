(ns app.profiles.resolvers
  (:require
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]))

(defresolver profile-resolver [{:keys [db]} {:profile/keys [id]}]
  {::pc/input #{:profile/id}
   ::pc/output [:profile/name]}
  (crux/entity db id))

(def resolvers [profile-resolver])
