(ns app.posts.ui.new-comment-button
  (:require
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2 button]]
    [app.posts.ui.comment-form :refer [ui-comment-form add-comment]]
    [com.fulcrologic.fulcro.components :as comp]))

(defn ui-new-comment-button [this new-comment post-id parent-id]
  (div :.ui.container.segment
       (if (empty? new-comment)
         (button :.ui.button {:onClick #(comp/transact! this `[(add-comment {:post-id ~post-id :parent-id ~parent-id})])} "Add comment")
         (ui-comment-form new-comment {:post-id post-id :parent-id parent-id}))))
