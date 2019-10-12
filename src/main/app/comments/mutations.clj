(ns app.comments.mutations
  (:require
    [app.database.crux :refer [get-entities]]
    [app.comments.validations :refer [valid-comment?]]
    [app.auth.resolvers :as auth]
    [app.posts.mutations :refer [random-name]]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn get-post-profile [])

(defn create-comment [crux-node account {:comment/keys [body post parent] :as comment}]
  (let [comment-id (util/uuid)
        profile-id (util/uuid)]
    (if (valid-comment? comment)
      (do
        (crux/submit-tx
          crux-node
          [[:crux.tx/put
            {:crux.db/id comment-id
             :comment/body body
             :comment/post post
             :comment/parent parent
             :comment/profile profile-id
             :comment/account account}]
           [:crux.tx/put
            {:crux.db/id profile-id
             :profile/name (random-name)
             :profile/account account}]])
        comment-id)
      (throw (ex-info "Comment validation failed" comment)))))

(defmutation create-comment! [{:keys [crux-node] :as env} {:comment/keys [tempid] :as comment}]
  {::pc/sym 'app.comments.ui.comment-form/create-comment!
   ::pc/input #{:comment/body :comment/post :comment/parent}
   ::pc/output [:comment/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      {:tempids {tempid (create-comment crux-node account-id comment)}}
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))
