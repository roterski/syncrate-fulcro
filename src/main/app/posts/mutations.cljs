(ns app.posts.mutations
  (:require
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]
    [app.posts.helpers :refer [post-form-ident post-form-class comment-form-ident comment-form-class]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))


