(ns app.posts.resolvers
  (:require
    [app.posts.mutations :refer [create-post!]]
    [app.model.database :refer [node]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver all-posts-resolver [env input]
             {::pc/output [{:all-posts [:post-list/id]}]}
             {:all-posts {:post-list/id :all-posts}})

(defresolver list-resolver [env {:post-list/keys [id]}]
             {::pc/input #{:post-list/id}
              ::pc/output [:post-list/label {:post-list/posts [:post/id]}]}
             (let [post-ids (crux/q (crux/db node)
                                    `{:find [e]
                                      :where [[e :post/title _]]})]
               {:post-list/id id
                :post-list/label "All Posts"
                :post-list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))

(defresolver post-resolver [env {:post/keys [id]}]
             {::pc/input #{:post/id}
              ::pc/output [:post/title :post/body]}
             (crux/entity (crux/db node) id))

(def resolvers [all-posts-resolver list-resolver post-resolver create-post!])