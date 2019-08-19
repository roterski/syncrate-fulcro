(ns app.posts.mutations
  (:require
    [app.database.crux :refer [node get-entities]]
    [app.auth.resolvers :as auth]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defn random-name []
  (clojure.string/join " "[(tt/quality) (tt/color) (tt/animal)]))

(defn create-post-with-profile [title body account-id]
  (let [post-id (keyword "post.id" (str (util/uuid)))
        profile-uuid (keyword "profile.id" (str (util/uuid)))]
    (do
      (crux/submit-tx
        node
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

(defmutation create-post! [env {:keys [title body]}]
  {::pc/sym `create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (let [account-id (get-in env [:ring/request :session :account/id])]
    (if account-id
      (create-post-with-profile title body account-id)
      (throw (ex-info "Unauthorized" {:status-code 403 :message "not authenticated"})))))
