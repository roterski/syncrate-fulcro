(ns app.comments.resolvers
  (:require
    [app.comments.mutations :refer [create-comment!]]
    [app.database.crux :refer [get-entities]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver comment-resolver [{:keys [db]} {:comment/keys [id]}]
  {::pc/input #{:comment/id}
   ::pc/output [:comment/body :comment/post :comment/parent :comment/profile]}
  (let [comment (crux/entity db id)
        profile-ident (if-let [profile-id (:comment/profile comment)]
                        {:profile/id profile-id}
                        nil)]
    (assoc comment :comment/profile profile-ident)))

(defresolver comment-children-resolver [{:keys [db]} {:comment/keys [id]}]
  {::pc/input #{:comment/id}
   ::pc/output [:comment/children]}
  (let [children-query `{:find [?e]
                         :where [[?e :comment/parent ~id]]}
        children (mapv (fn [id] {:comment/id (first id)}) (crux/q db children-query))]
    {:comment/children children}))

(def resolvers [comment-resolver comment-children-resolver create-comment!])
