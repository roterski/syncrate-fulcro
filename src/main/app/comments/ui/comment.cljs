(ns app.comments.ui.comment
  (:require
    [app.comments.ui.comment-form :refer [ui-comment-form]]
    [app.auth.ui.session :refer [Session]]
    [app.comments.ui.new-comment-button :refer [ui-new-comment-button]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(declare ui-comment)

(defsc Comment [this {:comment/keys [id body post-id children new-comment] :keys [current-session] :as props}]
  {:query (fn [] [:comment/id :comment/body :comment/post-id
                  {:comment/new-comment '...}
                  {:comment/children '...}
                  {[:current-session '_] (comp/get-query Session)}])
   :ident :comment/id}
  (div :.ui.container.segment
    body
    (when (:session/valid? current-session)
      (ui-new-comment-button this {:new-comment new-comment :post-id post-id :parent-id id}))
    (when (seq children)
      (div
        (dom/ul
          (map
           ui-comment
           children))))))

(def ui-comment (comp/factory Comment {:keyfn #(-> % :comment/id str)}))
