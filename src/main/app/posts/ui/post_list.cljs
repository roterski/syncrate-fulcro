(ns app.posts.ui.post-list
  (:require
    [app.posts.ui.post :refer [Post ui-post]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]))

(defsc PostList [this {:post-list/keys [id label posts] :as props}]
  {:query [:post-list/id :post-list/label {:post-list/posts (comp/get-query Post)}]
   :ident :post-list/id
   :route-segment ["post-list" :post-list/id]
   ;:will-enter (fn [_ {:post-list/keys [id]}] (dr/route-immediate [:post-list/id (keyword id)]))}
   :will-enter (fn [app {:post-list/keys [id]}]
                 (let [id (keyword id)]
                   (dr/route-deferred [:post-list/id id]
                      #(df/load app [:post-list/id id] PostList
                                {:post-mutation `dr/target-ready
                                 :post-mutation-params {:target [:post-list/id id]}}))))}
  (div :.ui.container.segment
    (h1 label)
    (when posts
      (map ui-post posts))))