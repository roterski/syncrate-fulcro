(ns app.posts.resolvers
  (:require
    [app.posts.mutations :refer [create-post!]]
    [app.database.crux :refer [node get-entities]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver list-resolver [env {:post-list/keys [id]}]
  {::pc/input #{:post-list/id}
   ::pc/output [:post-list/label {:post-list/posts [:post/id]}]}
  (let [post-ids (crux/q (crux/db node)
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

(defresolver post-resolver [env {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/title :post/body :post/author]}
  (crux/entity (crux/db node) id))

(defresolver profile-resolver [env {:profile/keys [id]}]
  {::pc/input #{:profile/id}
   ::pc/output [:profile/name]}
  (crux/entity (crux/db node) id))

(def resolvers [list-resolver post-resolver create-post! profile-resolver])

(comment
  (all-profiles)
  (all-posts))
