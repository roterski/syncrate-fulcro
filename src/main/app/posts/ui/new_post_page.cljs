(ns app.posts.ui.new-post-page
  (:require
    [app.posts.ui.post-form :refer [ui-post-form]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

(defsc NewPostPage [this props]
   {:ident         (fn [] [:component/id :new-post-page])
    :query         []
    :route-segment ["new-post"]
      :will-enter        (fn [app _] (dr/route-immediate [:component/id :new-post-page]))}
   (div
     (dom/h3 "New Post")
     (ui-post-form {})))

