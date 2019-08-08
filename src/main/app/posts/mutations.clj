(ns app.posts.mutations
  (:require
    [app.model.database :refer [node]]
    [app.util :as util]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn create-post [title body author-id]
  (crux/submit-tx
    node
    [[:crux.tx/put
      {:crux.db/id (keyword "post.id" (str (util/uuid)))
       :post/title title
       :post/body body
       :post/author author-id}]]))

(defmutation create-post! [env {:keys [title body]}]
  {::pc/sym `create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (create-post title body account-id)))
