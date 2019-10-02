(ns app.comments.mutations
  (:require
    [app.database.crux :refer [get-entities]]
    [app.comments.validations :refer [valid-comment?]]
    [app.auth.resolvers :as auth]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn create-comment [crux-node account-id {:comment/keys [body post-id parent-id] :as comment}]
  (let [comment-id (util/uuid)]
    (if (valid-comment? comment)
      (do
        (crux/submit-tx
          crux-node
          [[:crux.tx/put
            {:crux.db/id comment-id
             :comment/body body
             :comment/post-id post-id
             :comment/parent-id parent-id
             :comment/account-id account-id}]])
        comment-id)
      (throw (ex-info "Comment validation failed" comment)))))

(defmutation create-comment! [{:keys [crux-node] :as env} {:comment/keys [tempid body post-id parent-id] :as comment}]
  {::pc/sym 'app.comments.ui.comment-form/create-comment!
   ::pc/input #{:comment/body :comment/post-id :comment/parent-id}
   ::pc/output [:comment/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      {:tempids {tempid (create-comment crux-node account-id comment)}}
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))
