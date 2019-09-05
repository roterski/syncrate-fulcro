(ns app.posts.validations
  (:require
    #?(:clj [clojure.spec.alpha :as s]
       :cljs [cljs.spec.alpha :as s])))

(s/def :post/title #(<= 3 (count %)))
(s/def :post/body #(< 0 (count %)))

(s/def ::post (s/keys :req [:post/title :post/body]))
(defn valid-post? [attrs] (s/valid? ::post attrs))

(s/def :comment/body #(< 0 (count %)))
(s/def :comment/post-id #(= "post.id" (namespace %)))
(s/def :comment/parent-id (s/or
                            :root nil?
                            :child #(= "comment.id" (namespace %))))

(s/def ::comment (s/keys :req [:comment/body :comment/post-id :comment/parent-id]))
(defn valid-comment? [attrs] (s/valid? ::comment attrs))
