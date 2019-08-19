(ns app.posts.ui.comment
  (:require
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]))


(declare ui-comment)

(defsc Comment [this {:keys [:db/id :comment/body :comment/children]}]
  {:query (fn [] [:db/id :comment/body {:comment/children '...}])
   :ident [:comment/id :db/id]}
  (div :.ui.container.segment
    body
    (when (seq children)
      (div
          (dom/ul
            (map (fn [p]
                  (ui-comment p))
                children))))))

(def ui-comment (comp/factory Comment {:keyfn :db/id}))
