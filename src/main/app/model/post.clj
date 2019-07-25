(ns app.model.post
  (:require
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [ghostwheel.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

(>defn all-post-ids
  "Returns a sequence of UUIDs for all of the posts in the system"
  [db]
  [any? => (s/coll-of uuid? :kind vector?)]
  (d/q '[:find [?v ...]
         :where
         [?e :post/id ?v]]
    db))

(defresolver all-posts-resolver [{:keys [db]} input]
             {;;GIVEN nothing (e.g. this is usable as a root query)
              ;; I can output all accounts. NOTE: only ID is needed...other resolvers resolve the rest
              ::pc/output [{:all-posts [:post/id]}]}
             {:all-posts (mapv
                           (fn [id] {:post/id id})
                           (all-post-ids db))})

(>defn get-post [db id subquery]
       [any? uuid? vector? => (? map?)]
       (d/pull db subquery [:post/id id]))

(defresolver post-resolver [{:keys [db] :as env} {:post/keys [id]}]
             {::pc/input  #{:post/id}
              ::pc/output [:post/title :post/body]}
             (get-post db id [:post/title :post/body]))

(def resolvers [all-posts-resolver post-resolver])
