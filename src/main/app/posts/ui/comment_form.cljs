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
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc CommentForm [this {:comment/keys [id body] :as props} {:keys [post-id]}]
  {:query             [:comment/id :comment/body fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config CommentForm
                          {:comment/body  ""}))
   :form-fields       #{:comment/body}
   :ident             (fn [] comment-form-ident)
   :componentDidMount (fn [this]
                        (comp/transact! this [(pm/clear-comment-form)]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(pm/create-comment! {:body body :post-id post-id})])
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
          "Create")))))

(def ui-comment-form (comp/computed-factory CommentForm))
