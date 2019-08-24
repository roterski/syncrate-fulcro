(ns app.posts.ui.comment
  (:require
    [app.posts.ui.comment-form :refer [ui-comment-form]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]))

(declare ui-comment)

(defsc Comment [this {:comment/keys [id body post-id children]}]
  {:query (fn [] [:comment/id :comment/body :comment/post-id
                  {:comment/children '...}])
   :ident :comment/id}
  (div :.ui.container.segment
    body
    (ui-comment-form {} {:post-id post-id :parent-id id})
    (when (seq children)
      (div
        (dom/ul
          (map
           (fn [p] (ui-comment p))
           children))))))

(def ui-comment (comp/factory Comment {:keyfn :comment/id}))
