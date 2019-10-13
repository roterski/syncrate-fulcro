(ns app.comments.mutations
  (:require
    [app.database.crux :refer [get-entities node]]
    [app.comments.validations :refer [valid-comment?]]
    [app.auth.resolvers :as auth]
    [app.posts.mutations :refer [random-name]]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn create-comment [crux-node account {:comment/keys [body post parent] :as comment}]
  (let [comment-id (util/uuid)
        profile-id (util/uuid)
        account-id (util/uuid account)]
    (if (valid-comment? comment)
      (do
        (crux/submit-tx
          crux-node
          [[:crux.tx/put
            {:crux.db/id comment-id
             :comment/body body
             :comment/post (util/uuid post)
             :comment/parent (when parent (util/uuid parent))
             :comment/profile profile-id
             :comment/account account-id}]
           [:crux.tx/put
            {:crux.db/id profile-id
             :profile/name (random-name)
             :profile/account account-id}]])
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

(comment
  (do
    (def db (crux/db node))
    (defn all-profiles []
      (get-entities `{:find [?e]
                      :where [[?e :profile/name _]]}))
    (defn all-posts []
      (get-entities `{:find [?e]
                      :where [[?e :post/title _]]}))
    (defn all-accounts []
      (get-entities `{:find [?e]
                      :where [[?e :account/email _]]}))
    (defn all-comments []
      (get-entities `{:find [?e]
                      :where [[?e :comment/body _]]}))
    (defn all-post-comments [post-id]
      (get-entities `{:find [?e]
                      :where [[?e :comment/post ~post-id]]}))
    (defn all-post-profiles [post-id account-id]
      (->> [`{:find [?profile]
              :where [[?comment :comment/post ~post-id]
                      [?comment :comment/profile ?profile]
                      [?profile :profile/account ~account-id]]}
             `{:find [?profile]
               :where [[?post :crux.db/id ~post-id]
                       [?post :post/profile ?profile]
                       [?profile :profile/account ~account-id]]}]
          (map (fn [query] (crux/q db query)))
          (apply clojure.set/union)
          (map (fn [[id]] (crux/entity db id)))))))

(comment
  (all-posts)
  (all-profiles)
  (all-accounts)
  (do
    (def account-id #uuid"358fe9ea-ac95-4978-bc8a-a60f1bd9f3ea")
    (def post-id #uuid"0b1dd576-5806-454c-847d-c403ea524b33")
    ;(all-post-comments post-id)
    (all-post-profiles post-id account-id)))
