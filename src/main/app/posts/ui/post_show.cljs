(ns app.posts.ui.post-show
  (:require
   [app.posts.ui.post :refer [Post ui-post]]
   [app.posts.ui.profile :refer [Profile]]
   [app.posts.ui.comment :refer [ui-comment Comment]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2]]
   [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [taoensso.timbre :as log]
   [com.fulcrologic.fulcro-css.css :as css]))

(defsc PostShow [this {:post/keys [id title body author comments] :as post}]
  {:query [:post/id :post/title :post/body {:post/author (comp/get-query Profile)}
           {:post/comments (comp/get-query Comment)}]
   :ident :post/id
   :route-segment ["post-show" :post/id]
  ;  :will-enter (fn [_ {:post/keys [id]}] (dr/route-immediate [:post/id (keyword id)]))}
   :will-enter (fn [app {:post/keys [id]}]
                 (let [id (keyword id)]
                   (dr/route-deferred [:post/id id]
                                      #(df/load app [:post/id id] PostShow
                                                {:post-mutation `dr/target-ready
                                                 :post-mutation-params {:target [:post/id id]}}))))}
  (div :.ui.container.segment
    (h1 "Post")
    (ui-post post)
    (h2 "Comments")
    (ui-comment comments)
    ))
