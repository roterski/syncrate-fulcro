(ns app.posts.ui.comment-form
  (:require
    [app.posts.mutations :as pm]
    [app.posts.helpers :refer [comment-form-ident]]
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.tempid :refer [tempid]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(defn add-comment*
  [state-map {:keys [id body post-id parent-id]}]
  (let [comment-ident [:comment/id id]
        new-comment-entity {:comment/id id :comment/body body :comment/post-id post-id :comment/parent-id parent-id}
        parent-ident (if (nil? parent-id)
                       [:post/id post-id :post/comments]
                       [:comment/id parent-id :comment/children])]
    (-> state-map
        (update-in parent-ident (fnil conj []) comment-ident)
        (assoc-in comment-ident new-comment-entity))))

(declare CommentForm)

(defmutation add-comment
  [{:keys [post-id parent-id]}]
  (action [{:keys [state]}]
    (let [comment-id (tempid)]
      (swap! state (fn [s]
                     (-> s
                         (add-comment* {:id comment-id :body "" :post-id post-id :parent-id parent-id})
                         (fs/add-form-config* CommentForm [:comment/id comment-id])))))))

(defn remove-comment*
  [state-map {:keys [id post-id parent-id]}]
  (let [remove-fn (fn [c] (vec (remove #(= id (second %)) c)))]
    (cond-> state-map
      true (update-in [:comment/id] dissoc id)
      (some? post-id) (update-in [:post/id post-id :post/comments] remove-fn)
      (some? parent-id) (update-in [:comment/id parent-id :comment/children] remove-fn))))

(defmutation remove-comment
  [props]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                       (remove-comment* props))))))

(defsc CommentForm [this {:comment/keys [id body] :as props} {:keys [post-id parent-id]}]
  {:query             [:comment/id :comment/body fs/form-config-join]
   ;:initial-state     (fn [_]
   ;                     (fs/add-form-config CommentForm
   ;                       {:comment/body  ""}))
   :form-fields       #{:comment/body}
   :ident             :comment/id}
   ;:componentDidMount (fn [this]
   ;                     (comp/transact! this [(pm/clear-comment-form)]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(pm/create-comment! {:body body :post-id post-id :parent-id parent-id})])
                     (log/info "Create comment")))
        checked? (fs/checked? props)]
    (div
      (dom/h3 "New Comment")
      (div :.ui.form {:classes [(when checked? "error")]}
        (field {:label         "Body"
                :value         (or body "")
                :onKeyDown     submit!
                :autoComplete  "off"
                :onChange      #(m/set-string! this :comment/body :event %)})
        (dom/button :.ui.primary.button {:onClick #(submit! true)}
          "Create")
        (dom/button :.ui.secondary.button {:onClick #(comp/transact! this `[(remove-comment {:id ~id :post-id ~post-id :parent-id ~parent-id})])} "Cancel")))))

(def ui-comment-form (comp/computed-factory CommentForm {:keyfn :comment/id}))
