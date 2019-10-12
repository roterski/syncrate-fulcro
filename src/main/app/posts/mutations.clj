(ns app.posts.mutations
  (:require
    [app.database.crux :refer [get-entities]]
    [app.posts.validations :refer [valid-post?]]
    [app.auth.resolvers :as auth]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn random-name []
  (clojure.string/join " "[(tt/quality) (tt/color) (tt/animal)]))

(defn create-post [crux-node account-id {:post/keys [title body] :as post}]
  (let [post-id (util/uuid)
        profile-uuid (util/uuid)]
    (if (valid-post? post)
      (do
        (crux/submit-tx
          crux-node
          [[:crux.tx/put
            {:crux.db/id post-id
             :post/title title
             :post/body body
             :post/profile profile-uuid}]
           [:crux.tx/put
            {:crux.db/id profile-uuid
             :profile/name (random-name)
             :profile/account account-id}]])
        post-id)
      (throw (ex-info "Post validation failed" post)))))

(defmutation create-post! [{:keys [crux-node] :as env} {:post/keys [tempid title body] :as post}]
  {::pc/sym `app.posts.ui.post-form/create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      {:tempids {tempid (create-post crux-node account-id post)}}
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))
