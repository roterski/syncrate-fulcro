(ns app.posts.mutations
  (:require
    [app.database.crux :refer [get-entities]]
    [app.auth.resolvers :as auth]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn random-name []
  (clojure.string/join " "[(tt/quality) (tt/color) (tt/animal)]))

(defn create-post [crux-node title body account-id]
  (let [post-id (keyword "post.id" (str (util/uuid)))
        profile-uuid (keyword "profile.id" (str (util/uuid)))]
    (do
      (crux/submit-tx
        crux-node
        [[:crux.tx/put
          {:crux.db/id post-id
           :post/title title
           :post/body body
           :post/author {:profile/id profile-uuid}}]
         [:crux.tx/put
          {:crux.db/id profile-uuid
           :profile/name (random-name)
           :profile/account {:account/id account-id}}]])
      post-id)))

(defmutation create-post! [{:keys [crux-node] :as env} {:keys [title body]}]
  {::pc/sym `create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      (create-post crux-node title body account-id)
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))

(defn create-comment [crux-node body post-id parent-id account-id]
  (let [comment-id (keyword "comment.id" (str (util/uuid)))]
    (do
      (crux/submit-tx
        crux-node
        [[:crux.tx/put
          {:crux.db/id comment-id
           :comment/body body
           :comment/post-id post-id
           :comment/parent-id parent-id
           :comment/account-id account-id}]])
      comment-id)))

(defmutation create-comment! [{:keys [crux-node] :as env} {:keys [tempid body post-id parent-id]}]
  {::pc/sym 'app.posts.ui.comment-form/create-comment!
   ::pc/input #{:comment/body :comment/post-id :comment/parent-id}
   ::pc/output [:comment/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      {:tempids {tempid (create-comment crux-node body post-id parent-id account-id)}}
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))
