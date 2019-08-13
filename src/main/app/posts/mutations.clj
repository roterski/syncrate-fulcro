(ns app.posts.mutations
  (:require
    [app.model.database :refer [node get-entities]]
    [app.util :as util]
    [crux.api :as crux]
    [talltale.core :as tt]
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
           :post/profile {:profile/id profile-uuid}}]
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
    (create-post-with-profile title body account-id)))
