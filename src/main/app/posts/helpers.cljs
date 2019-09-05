(ns app.posts.helpers
  (:require
    [com.fulcrologic.fulcro.components :as comp]))

(def post-form-ident [:component/id :post-form])
(defn post-form-class [] (comp/registry-key->class :app.posts.ui.post-form/PostForm))

(def comment-form-ident [:component/id :comment-form])
(defn comment-form-class [] (comp/registry-key->class :app.posts.ui.comment-form/CommentForm))
