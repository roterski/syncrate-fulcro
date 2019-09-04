(ns app.posts.ui.new-post-page
  (:require
    [app.posts.ui.post-form :refer [ui-post-form PostForm add-post-form]]
    [app.posts.ui.post :refer [Post]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.algorithms.tempid :refer [tempid]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.components :as comp]))

(defsc NewPostPage [this {:new-post-page/keys [post] :as props}]
   {:ident         (fn [] [:component/id :new-post-page])
    :query         [{:new-post-page/post (comp/get-query Post)}]
    :route-segment ["new-post"]
    :will-enter        (fn [app _] (dr/route-immediate [:component/id :new-post-page]))}
   (when-not post
     (comp/transact! this `[(add-post-form {})]))
   (div
     (dom/h3 "New Post")
     (div
       (when post
         (ui-post-form post)))))

