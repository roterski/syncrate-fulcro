(ns app.posts.mutations
  (:require
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]
    [app.posts.helpers :refer [post-form-ident post-form-class comment-form-ident comment-form-class]]
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
    (log/info "Marking post complete")
    (swap! state fs/mark-complete* post-form-ident))
  (ok-action [{:keys [app state] :as params}]
    (dr/change-route app ["post-list" "all-posts"]))
  (error-action [env]
    (log/error "Post creation failed")
    (log/error env))
  (remote [{:keys [state] :as env}] true))

(defn clear-comment-form*
  [state-map]
  (-> state-map
      (assoc-in (conj comment-form-ident :comment/body) "")
      (fs/add-form-config* (comment-form-class) comment-form-ident)))

(defmutation clear-comment-form [_]
  (action [{:keys [state]}]
    (swap! state clear-comment-form*)))

(defmutation create-comment! [{:keys [tempid]}]
  (action [{:keys [state]}]
    (log/info "Marking comment complete")
    (swap! state fs/mark-complete* [:comment/id tempid]))
  (error-action [env]
    (log/error "Comment creating failed")
    (log/error env))
  (remote [{:keys [state] :as env}] true))
