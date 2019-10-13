(ns app.comments.ui.new-comment-button
  (:require
    [app.comments.ui.comment-form :refer [ui-comment-form add-comment-form]]
    ;[app.comments.ui.comment :refer [Comment]]
    [app.auth.ui.session :refer [Session]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2 button]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]))

(defn ui-new-comment-button [this {:keys [new-comment post parent]}]
  (div :.ui.container.segment
    (if (empty? new-comment)
     (button :.ui.button {:onClick #(comp/transact! this `[(add-comment-form {:comment/post ~post :comment/parent ~parent})])} "Add comment")
     (ui-comment-form new-comment {:post post :parent parent}))))
