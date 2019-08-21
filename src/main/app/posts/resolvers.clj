(ns app.posts.resolvers
  (:require
    [app.posts.mutations :refer [create-post!]]
    [app.database.crux :refer [get-entities]]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver list-resolver [{:keys [db]} {:post-list/keys [id]}]
  {::pc/input #{:post-list/id}
   ::pc/output [:post-list/label {:post-list/posts [:post/id]}]}
  (let [post-ids (crux/q db
                         `{:find [e]
                           :where [[e :post/title _]]})]
    {:post-list/id id
     :post-list/label "All Posts"
     :post-list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))


(defn all-profiles []
  (get-entities `{:find [?e]
                  :where [[?e :profile/name _]]}))

(defn all-posts []
  (get-entities `{:find [?e]
                  :where [[?e :post/title _]]}))

(defn get-account-profiles [account-id]
  (get-entities `{:find [?e]
                  :where [[?e :profile/account ~account-id]]}))

; ----------
;  TEMP
(defn make-comment
  [id body children]
  (cond-> {:comment/id id :comment/body body}
    children (assoc :comment/children children)))

(def fake-comments
  (make-comment 1 "Server says Hi"
                [(make-comment 3 "Hey" [(make-comment 4 "Aloha" [(make-comment 5 "halko" [])])
                                        (make-comment 6 "elko" [])
                                        (make-comment 7 "bobb" [])])
                 (make-comment 8 "robb" [])
                 (make-comment 9 "Hesooy" [])]))
; TEMP
; ----------

(defresolver post-resolver [{:keys [db]} {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/title :post/body :post/author :post/comments]}
  (merge (crux/entity db id)
    {:post/comments fake-comments}))

(defresolver profile-resolver [{:keys [db]} {:profile/keys [id]}]
  {::pc/input #{:profile/id}
   ::pc/output [:profile/name]}
  (crux/entity db id))

(def resolvers [list-resolver post-resolver create-post! profile-resolver])

(comment
  (all-profiles)
  (all-posts))
