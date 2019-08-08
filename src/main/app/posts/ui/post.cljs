(ns app.posts.ui.post
  (:require
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc Post [this {:post/keys [title body] :as props}]
       {:query [:post/id :post/title :post/body]
        :ident (fn [] [:post/id (:post/id props)])}
       (dom/div :.ui.container.segment
                     (dom/h5 title)
                     body))

(def ui-post (comp/factory Post {:keyfn :post/id}))
