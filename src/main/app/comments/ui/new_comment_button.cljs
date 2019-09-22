(ns app.comments.ui.new-comment-button
  (:require
    [app.comments.ui.comment-form :refer [ui-comment-form add-comment-form]]
    ;[app.comments.ui.comment :refer [Comment]]
    [app.auth.ui.session :refer [Session]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2 button]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]))

(defn ui-new-comment-button [this {:keys [new-comment post-id parent-id]}]
  (div :.ui.container.segment
    (if (empty? new-comment)
     (button :.ui.button {:onClick #(comp/transact! this `[(add-comment-form {:comment/post-id ~post-id :comment/parent-id ~parent-id})])} "Add comment")
     (ui-comment-form new-comment {:post-id post-id :parent-id parent-id}))))
