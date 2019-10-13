(ns app.comments.validations
  (:require
    #?(:clj [clojure.spec.alpha :as s]
       :cljs [cljs.spec.alpha :as s])))

(s/def :comment/body (s/and string? #(< 0 (count %))))
(s/def :comment/post (s/or :string string? :uuid uuid?))
(s/def :comment/parent (s/or
                         :root nil?
                         :child (s/or :string string? :uuid uuid?)))

(s/def ::comment (s/keys :req [:comment/body :comment/post :comment/parent]))
(defn valid-comment? [attrs] (s/valid? ::comment attrs))
