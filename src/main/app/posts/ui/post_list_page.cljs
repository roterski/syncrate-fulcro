(ns app.posts.ui.post-list-page
  (:require
    [app.posts.ui.post :refer [Post ui-post]]
    [app.util :refer [parse-int]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]))

(defn paginator [component page-number]
  (let [current-page (parse-int page-number)
        next-page (inc current-page)
        previous-page (dec current-page)]
    (div "Page")
    (div
      (if (> previous-page 0)
        (dom/a :.item {:href (str "/post-list/all-posts/page/" previous-page)} " < ")
        " | ")
      current-page
      (dom/a :.item {:href (str "/post-list/all-posts/page/" next-page)} " > "))))

(defsc PostListPage [this {:post-list/keys [id label posts page-number] :as props}]
  {:query [:post-list/id :post-list/label :post-list/page-number {:post-list/posts (comp/get-query Post)}]
   :ident :post-list/id
   :route-segment ["post-list" :post-list/id "page" :post-list/page-number]
   :will-enter (fn [app {:post-list/keys [id page-number]}]
                 (let [id (keyword id)]
                   (dr/route-deferred [:post-list/id id]
                      #(df/load! app [:post-list/id id] PostListPage
                                {:post-mutation `dr/target-ready
                                 :params {:page-number page-number}
                                 :post-mutation-params {:target [:post-list/id id]}}))))}
  (div :.ui.container.segment
    (h1 label)
    (when posts
      (map ui-post posts))
    (paginator this page-number)))
