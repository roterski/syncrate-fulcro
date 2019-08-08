(ns app.posts.mutations
  (:require
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]
    [app.posts.helpers :refer [post-form-ident post-form-class]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defn clear-post-form*
  [state-map]
  (-> state-map
    (assoc-in post-form-ident
      {:post/title ""
       :post/body  ""})
    (fs/add-form-config* (post-form-class) post-form-ident)))

(defmutation clear-post-form [_]
  (action [{:keys [state]}]
    (swap! state clear-post-form*)))

(defmutation create-post! [_]
  (action [{:keys [state]}]
    (log/info "Marking complete")
    (swap! state fs/mark-complete* post-form-ident))
  (ok-action [{:keys [app state]}]
    (dr/change-route app ["post-list" "all-posts"]))
  (remote [{:keys [state] :as env}] true))

