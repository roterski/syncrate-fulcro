(ns app.posts.ui.post-form
  (:require
    [cljs.spec.alpha :as s]
    [app.posts.mutations :as post]
    [app.posts.validations]
    [app.posts.helpers :refer [post-form-ident]]
    [app.ui.components :refer [field]]
    [clojure.set :refer [intersection subset?]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.tempid :refer [tempid]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
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

(defmutation create-post! [{:post/keys [tempid]}]
  (action [{:keys [state]}]
    (log/info "Creating post..."))
  (ok-action [{:keys [app state result] :as params}]
    (log/info "...post created successfully!")
    (swap! state (fn [s]
                   (-> s
                       (update-in [::fs/forms-by-ident] dissoc {:table :post/id :row tempid})
                       (assoc-in [:component/id :new-post-page :new-post-page/post] nil))))
    (dr/change-route app ["post-list" "all-posts"]))
  (error-action [env]
    (log/error "...creating post failed!")
    (log/error env))
  (remote [{:keys [state] :as env}] true))

(def not-empty? (complement empty?))

(defsc PostForm [this {:post/keys [id title body] :as props}]
  {:query             [:post/id :post/title :post/body fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config PostForm
                          {:post/title ""
                           :post/body  ""}))
   :form-fields       #{:post/title :post/body}
   :ident             :post/id}
  (let [title-validity (fs/get-spec-validity props :post/title)
        body-validity (fs/get-spec-validity props :post/body)
        validity (conj #{} title-validity body-validity)
        submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this `[(fs/mark-complete! {})])
                     (when (= #{:valid} validity)
                       (comp/transact! this `[(create-post! {:post/tempid ~id :post/title ~title :post/body ~body})]))))
        cancel #(comp/transact! this [(clear-post-form {:id id})])]
    (div :.ui.form {:classes [(when (subset? #{:invalid} validity) "error")]}
      (field {:label         "Title"
              :value         (or title "")
              :valid?        (contains? #{:valid :unchecked} title-validity)
              :error-message "Must be at least 3 char long"
              :autoComplete  "off"
              :onBlur        #(comp/transact! this `[(fs/mark-complete! {:field :post/title})])
              :onKeyDown     submit!
              :onChange      #(do
                                (m/set-string! this :post/title :event %))})
      (field {:label         "Body"
              :value         (or body "")
              :valid?        (contains? #{:valid :unchecked} body-validity)
              :error-message "Cannot be blank"
              :onKeyDown     submit!
              :autoComplete  "off"
              :onChange      #(do
                                (comp/transact! this `[(fs/mark-complete! {:field :post/body})])
                                (m/set-string! this :post/body :event %))})
      (dom/button :.ui.primary.button {:onClick #(submit! true) :disabled (->> validity
                                                                               (intersection #{:invalid :unchecked})
                                                                               not-empty?)}
        "Create")
      (dom/button :.ui.secondary.button {:onClick cancel} "Cancel"))))

(def ui-post-form (comp/factory PostForm))
