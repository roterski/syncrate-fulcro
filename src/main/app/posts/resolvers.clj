(ns app.posts.resolvers
  (:require
    [app.posts.mutations :refer [create-post!]]
    [app.database.crux :refer [get-entities node]]
    [app.util :as util]
    [crux.api :as crux]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]))

(defresolver post-list-resolver [{:keys [db] :as env} {:post-list/keys [id]}]
  {::pc/input #{:post-list/id}
   ::pc/output [:post-list/label :post-list/page-number {:post-list/posts [:post/id]}]}
  (let [per-page 10
        [_ {:keys [page-number]}] (get-in env [:ring/request :transit-params 0])
        post-ids (crux/q db
                         `{:find   [e]
                           :where  [[e :post/title _]]
                           :limit  ~per-page
                           :offset ~(* (dec (util/parse-int page-number)) per-page)})]
    ; TODO: not optimal pagination, naively scrolls through the initial result set each time
    ; https://juxt.pro/crux/docs/queries.html#_ordering_and_pagination
      {:post-list/id id
       :post-list/label "All Posts"
       :post-list/page-number page-number
       :post-list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))

(defresolver post-resolver [{:keys [db]} {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/title :post/body :post/profile]}
  (let [post (crux/entity db id)
        profile-ident (if-let [profile-id (:post/profile post)]
                        {:profile/id profile-id}
                        nil)]
    (assoc post :post/profile profile-ident)))

(defresolver post-comments-resolver [{:keys [db]} {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/comments]}
  (let [comment-query {:find '[?e]
                       :where '[[?e :comment/parent parent-id]
                                [?e :comment/post post-id]]
                       :args [{'parent-id nil
                               'post-id (util/uuid id)}]}
        comment-ids (mapv (fn [id] {:comment/id (first id)}) (crux/q db comment-query))]
       {:post/comments comment-ids}))


(def resolvers [post-list-resolver post-resolver post-comments-resolver create-post!])

(comment
  (defn all-profiles []
    (get-entities `{:find [?e]
                    :where [[?e :profile/name _]]}))

  (defn all-posts []
    (get-entities `{:find [?e]
                    :where [[?e :post/title _]]}))

  (defn get-account-profiles [account-id]
    (get-entities `{:find [?e]
                    :where [[?e :profile/account ~account-id]]}))
  (all-profiles)
  (all-posts))
