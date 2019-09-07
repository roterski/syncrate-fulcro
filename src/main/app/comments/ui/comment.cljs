(ns app.comments.ui.comment
  (:require
    [app.comments.ui.comment-form :refer [ui-comment-form]]
    [app.comments.ui.new-comment-button :refer [ui-new-comment-button]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]))

(declare ui-comment)

(defsc Comment [this {:comment/keys [id body post-id children]}]
  {:query (fn [] [:comment/id :comment/body :comment/post-id
                  {:comment/children '...}])
   :ident :comment/id}
  (let [filter-fn #(tempid/tempid? (:comment/id %))
        new-comment (first (filter filter-fn children))
        saved-children (filter (complement filter-fn) children)]
    (div :.ui.container.segment
      body
      (when (not (tempid/tempid? id))
        (ui-new-comment-button this new-comment post-id id))
      (when (seq saved-children)
        (div
          (dom/ul
            (map
             (fn [p] (ui-comment p))
             saved-children)))))))

(def ui-comment (comp/factory Comment {:keyfn :comment/id}))
