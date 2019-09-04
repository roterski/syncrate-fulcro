(ns app.posts.ui.post-form
  (:require
    [app.posts.mutations :as post]
    [app.posts.helpers :refer [post-form-ident]]
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.tempid :refer [tempid]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(declare PostForm)

(defn add-post*
  [state-map {:post/keys [id] :as props}]
  (let [post-ident [:post/id id]]
    (-> state-map
      (assoc-in post-ident props)
      (assoc-in [:component/id :new-post-page :new-post-page/post] post-ident))))

(defmutation add-post-form
  [props]
  (action [{:keys [state]}]
    (let [post-id (tempid)]
      (log/info "Adding post form")
      (swap! state (fn [s]
                     (-> s
                       (add-post* (merge {:post/id post-id :post/body "" :post/title ""} props))
                       (fs/add-form-config* PostForm [:post/id post-id])))))))

(defn clear-post-form*
  [state-map id]
  (let [post-ident [:post/id id]
        form-ident {:table :post/id :row id}]
    (-> state-map
      (update-in post-ident
        merge
        {:post/title ""
         :post/body  ""})
      (assoc-in [::fs/forms-by-ident form-ident ::fs/complete?] #{}))))

(defmutation clear-post-form [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                       (clear-post-form* id))))))

(defsc PostForm [this {:post/keys [id title body] :as props}]
  {:query             [:post/id :post/title :post/body fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config PostForm
                          {:post/title ""
                           :post/body  ""}))
   :form-fields       #{:post/title :post/body}
   :ident             :post/id}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(post/create-post! {:title title :body body})])
                     (log/info "Create post")))
        cancel #(comp/transact! this [(clear-post-form {:id id})])
        checked? (log/spy :info (fs/checked? props))
        complete! (fn []
                    (do
                      (log/info "Completing post!")
                      (comp/transact! this `[(fs/mark-complete! {:field :post/title})])))]
    (div :.ui.form {:classes [(when checked? "error")]}
      (field {:label         "Title"
              :value         (or title "")
              :valid?        (>= 3 (count title))
              :error-message "Must be at least 3 char long"
              :autoComplete  "off"
              :onBlur        #(comp/transact! this `[(fs/mark-complete! {:field :post/title})])
              :onKeyDown     submit!
              :onChange      #(m/set-string! this :post/title :event %)})
      (field {:label         "Body"
              :value         (or body "")
              :onKeyDown     submit!
              :autoComplete  "off"
              :onChange      #(m/set-string! this :post/body :event %)})
      (dom/button :.ui.primary.button {:onClick #(submit! true)}
        "Create")
      (dom/button :.ui.secondary.button {:onClick cancel} "Cancel"))))

(def ui-post-form (comp/factory PostForm))
