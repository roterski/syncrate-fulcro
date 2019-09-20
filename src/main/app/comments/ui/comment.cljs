(ns app.comments.ui.comment
  (:require
    [app.comments.ui.comment-form :refer [ui-comment-form]]
    [app.comments.ui.new-comment-button :refer [ui-new-comment-button]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]))

(declare ui-comment)

(defsc Comment [this {:comment/keys [id body post-id children new-comment]}]
  {:query (fn [] [:comment/id :comment/body :comment/post-id
                  {:comment/new-comment '...}
                  {:comment/children '...}])
   :ident :comment/id}
  (div :.ui.container.segment
    body
    (when (not (tempid/tempid? id))
      (ui-new-comment-button this new-comment post-id id))
    (when (seq children)
      (div
        (dom/ul
          (map
           #(ui-comment %)
           children))))))

(def ui-comment (comp/factory Comment {:keyfn :comment/id}))
