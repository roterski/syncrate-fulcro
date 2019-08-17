(ns app.posts.ui.post
  (:require
    [app.ui.components :refer [field]]
    [app.posts.ui.profile :refer [Profile ui-profile]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Post [this {:post/keys [id title body author]}]
  {:query [:post/id
           :post/title
           :post/body
           {:post/author (comp/get-query Profile)}]
   :ident (fn [] [:post/id id])}
  (dom/div :.ui.container.segment
    (div
      (dom/h5 title)
      (p body)
      (ui-profile author))))

(def ui-post (comp/factory Post {:keyfn :post/id}))
