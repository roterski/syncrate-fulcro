(ns app.posts.ui.post-show-page
  (:require
   [app.posts.ui.post :refer [Post ui-post]]
   [app.profiles.ui.profile :refer [Profile]]
   [app.comments.ui.new-comment-button :refer [ui-new-comment-button]]
   [app.comments.ui.comment :refer [ui-comment Comment]]
   [app.comments.ui.comment-form :refer [ui-comment-form]]
   [app.application :refer [SPA]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.algorithms.denormalize :as dn]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.dom :as dom :refer [div h1 h2 button]]
   [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [taoensso.timbre :as log]
   [com.fulcrologic.fulcro-css.css :as css]))

(defsc PostShowPage [this {:post/keys [id title body author comments] :as post}]
  {:query [:post/id :post/title :post/body {:post/author (comp/get-query Profile)}
           {:post/comments (comp/get-query Comment)}]
   :ident :post/id
   :route-segment ["post-show" :id]
   :will-enter (fn [app {:keys [id]}]
                 (let [id (keyword "post.id" id)]
                   (dr/route-deferred [:post/id id]
                                      #(df/load app [:post/id id] PostShowPage
                                                {:post-mutation `dr/target-ready
                                                 :post-mutation-params {:target [:post/id id]}}))))}
  (let [filter-fn #(tempid/tempid? (:comment/id %))
        new-comment (first (filter filter-fn comments))
        saved-comments (filter (complement filter-fn) comments)]
    (div :.ui.container.segment
      (h1 "Post")
      (ui-post post)
      (ui-new-comment-button this new-comment id nil)
      (h2 "Comments")
      (map ui-comment saved-comments))))

(comment
  (let [state (app/current-state SPA)
        query (comp/get-query PostShow)
        post-id "5f7133f2-8701-46b6-9d30-a4f08e7f2e58"
        ident [:post/id (keyword "post.id" post-id)]
        post (dn/db->tree query ident state)
        comments (:post/comments post)
        comment-id (:comment/id (first comments))
        filter-fn (fn [comment] (tempid/tempid? (:comment/id comment)))
        temp-comments (filter filter-fn comments)
        temp-comment (first (filter filter-fn comments))
        saved-comments (filter (fn [comment] (not (filter-fn comment))) comments)]
    (def st state)))

