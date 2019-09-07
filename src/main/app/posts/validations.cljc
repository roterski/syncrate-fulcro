(ns app.posts.validations
  (:require
    #?(:clj [clojure.spec.alpha :as s]
       :cljs [cljs.spec.alpha :as s])))

(s/def :post/title #(<= 3 (count %)))
(s/def :post/body #(< 0 (count %)))

(s/def ::post (s/keys :req [:post/title :post/body]))
(defn valid-post? [attrs] (s/valid? ::post attrs))

