(ns app.comments.resolvers
  (:require
    [app.comments.mutations :refer [create-comment!]]
    [app.database.crux :refer [get-entities]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver comment-resolver [{:keys [db]} {:comment/keys [id]}]
             {::pc/input #{:comment/id}
              ::pc/output [:comment/body :comment/post-id :comment/parent-id :comment/children]}
             (let [children-query `{:find [?e]
                                    :where [[?e :comment/parent-id ~id]]}
                   children (mapv (fn [id] {:comment/id (first id)}) (crux/q db children-query))]
               (merge (crux/entity db id) {:comment/children children})))

(def resolvers [comment-resolver create-comment!])
