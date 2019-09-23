(ns app.comments.validations
  (:require
    #?(:clj [clojure.spec.alpha :as s]
       :cljs [cljs.spec.alpha :as s])))

(s/def :comment/body (s/and string? #(< 0 (count %))))
(s/def :comment/post-id string?)
(s/def :comment/parent-id (s/or
                            :root nil?
                            :child string?))

(s/def ::comment (s/keys :req [:comment/body :comment/post-id :comment/parent-id]))
(defn valid-comment? [attrs] (s/valid? ::comment attrs))
