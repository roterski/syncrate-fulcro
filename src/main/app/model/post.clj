(ns app.model.post
  (:require
    [app.model.mock-database :as db]
    [app.model.database :refer [node]]
    [app.util :as util]
    [crux.api :as crux]
    [datascript.core :as d]
    [ghostwheel.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

;(>defn all-post-ids
;  "Returns a sequence of UUIDs for all of the posts in the system"
;  [db]
;  [any? => (s/coll-of uuid? :kind vector?)]
;  (d/q '[:find [?v ...]
;         :where
;         [?e :post/id ?v]]
;    db))

(defn all-post-ids
  "Returns a sequence of UUIDs for all of the posts in the system"
  [db]
  (d/q '[:find [?v ...]
         :where
         [?e :post/id ?v]]
    db))

;(defresolver all-posts-resolver [{:keys [db]} input]
;             {;;GIVEN nothing (e.g. this is usable as a root query)
;              ;; I can output all accounts. NOTE: only ID is needed...other resolvers resolve the rest
;              ::pc/output [{:all-posts [:post/id]}]}
;             {:all-posts (mapv
;                           (fn [id] {:post/id id})
;                           (all-post-ids db))})


;(defresolver all-posts-resolver [{:keys [db]} input]
;             {;;GIVEN nothing (e.g. this is usable as a root query)
;              ;; I can output all accounts. NOTE: only ID is needed...other resolvers resolve the rest
;              ::pc/output [{:all-posts [:post/id]}]}
;             {:all-posts (mapv
;                           (fn [id] {:post/id id})
;                           (all-post-ids db))})


(defmutation create-post! [env {:keys [title body]}]
  {::pc/sym `create-post!
   ::pc/input #{:post/title :post/body}
   ::pc/output [:post/id]}
  (log/info "POST:" title "," body)
  (crux/submit-tx
    node
    [[:crux.tx/put
      {:crux.db/id (keyword "post.id" (str (util/uuid)))
       :post/title title
       :post/body body}]]))

;(>defn get-post [db id subquery]
;       [any? uuid? vector? => (? map?)]
;       (d/pull db subquery [:post/id id]))

;(defresolver all-posts-resolver [{:keys [db]} input]
;  {::pc/output [{:all-posts [:post/id]}]}
;  {:all-posts (crux/q (crux/db node)
;                `{:find [e]
;                  :where [[e :post/title _]]})})

(defresolver all-posts-resolver [env input]
  {::pc/output [{:all-posts [:list/id]}]}
  {:all-posts {:list/id :all-posts}})

(defresolver list-resolver [env {:list/keys [id]}]
  {::pc/input #{:list/id}
   ::pc/output [:list/label {:list/posts [:person/id]}]}
  (let [post-ids (crux/q (crux/db node)
                         `{:find [e]
                           :where [[e :post/title _]]})]
    {:list/id id
     :list/label "All Posts"
     :list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))

;(defresolver all-posts-resolver [env {:list/keys [id]}]
;  {::pc/input #{:list/id}
;   ::pc/output [:list/label {:list/posts [:person/id]}]}
;  (let [post-ids (crux/q (crux/db node)
;                         `{:find [e]
;                           :where [[e :post/title _]]})]
;    {:list/id :all-posts
;     :list/label "All Posts"
;     :list/posts (mapv (fn [id] {:post/id (first id)}) post-ids)}))

(defresolver post-resolver [env {:post/keys [id]}]
  {::pc/input #{:post/id}
   ::pc/output [:post/title :post/body]}
  (crux/entity (crux/db node) id))



;(defn get-post [db id subquery]
;  (d/pull db subquery [:post/id id]))
;
;(defresolver post-resolver [{:keys [db] :as env} {:post/keys [id]}]
;             {::pc/input  #{:post/id}
;              ::pc/output [:post/title :post/body]}
;             (get-post db id [:post/title :post/body]))

(def resolvers [all-posts-resolver list-resolver post-resolver create-post!])
