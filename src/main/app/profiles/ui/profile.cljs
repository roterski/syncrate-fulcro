(ns app.profiles.ui.profile
  (:require [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.dom :as dom :refer [div]]))

(defsc Profile [this {:profile/keys [id name] :as props}]
  {:query [:profile/id :profile/name]
   :ident :profile/id}
  (div "by " name))

(def ui-profile (comp/factory Profile {:keyfn #(-> % :profile/id str)}))
