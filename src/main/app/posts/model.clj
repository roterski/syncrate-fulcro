(ns app.posts.model
  (:require
    [app.model.mock-database :as db]
    [app.model.database :refer [node]]
    [app.util :as util]
    [crux.api :as crux]
    [datascript.core :as d]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

(defmutation create-post! [env {:keys [title body]}]
  {::pc/sym `create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (log/info "POST:" title "," body)
  (crux/submit-tx
    node
    [[:crux.tx/put
      {:crux.db/id (keyword "post.id" (str (util/uuid)))
       :post/title title
       :post/body body}]]))

(defresolver all-posts-resolver [env input]
  {::pc/output [{:all-posts [:post-list/id]}]}
  {:all-posts {:post-list/id :all-posts}})

(defresolver list-resolver [env {:post-list/keys [id]}]
  {::pc/input #{:post-list/id}
   ::pc/output [:post-list/label {:post-list/posts [:person/id]}]}
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
