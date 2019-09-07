(ns app.posts.resolvers
  (:require
    [app.posts.mutations :refer [create-post!]]
    [app.database.crux :refer [get-entities]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver list-resolver [{:keys [db]} {:post-list/keys [id]}]
  {::pc/input #{:post-list/id}
   ::pc/output [:post-list/label {:post-list/posts [:post/id]}]}
  (let [post-ids (crux/q db
                         `{:find [e]
                           :where [[e :post/title _]]})]
    {:post-list/id id
     :post-list/label "All Posts"
     :post-list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))

(defn all-profiles []
  (get-entities `{:find [?e]
                  :where [[?e :profile/name _]]}))

(defn all-posts []
  (get-entities `{:find [?e]
                  :where [[?e :post/title _]]}))

(defn get-account-profiles [account-id]
  (get-entities `{:find [?e]
                  :where [[?e :profile/account ~account-id]]}))

(defresolver post-resolver [{:keys [db]} {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/title :post/body :post/author :post/comments]}
  (let [comment-query {:find '[?e]
                       :where '[[?e :comment/parent-id parent-id]
                                [?e :comment/post-id post-id]]
                       :args [{'parent-id nil
                               'post-id id}]}
        comment-ids (mapv (fn [id] {:comment/id (first id)}) (crux/q db comment-query))]
    (merge (crux/entity db id)
      {:post/comments comment-ids})))

(def resolvers [list-resolver post-resolver create-post!])

(comment
  (all-profiles)
  (all-posts))
