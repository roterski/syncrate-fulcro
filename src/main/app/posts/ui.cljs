(ns app.posts.ui
  (:require
    [app.posts.model :as post]
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))


(defsc Post [this {:post/keys [title body] :as props}]
  {:query [:post/id :post/title :post/body]
   :ident (fn [] [:post/id (:post/id props)])}
  (dom/div :.ui.container.segment
    (dom/h5 title)
    body))

(def ui-post (comp/factory Post {:keyfn :post/id}))

(defsc PostForm [this {:post/keys [id title body] :as props}]
  {:query             [:post/id :post/title :post/body fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config PostForm
                          {:post/title ""
                           :post/body  ""}))
   :form-fields       #{:post/title :post/body}
   :ident             (fn [] post/post-form-ident)
   :route-segment     ["new-post"]
   :componentDidMount (fn [this]
                        (comp/transact! this [(post/clear-post-form)]))
   :will-enter        (fn [app _] (dr/route-immediate [:component/id :post-form]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(post/create-post! {:title title :body body})])
                     (log/info "Create post")))
        checked? (log/spy :info (fs/checked? props))]
    (div
      (dom/h3 "New Post")
      (div :.ui.form {:classes [(when checked? "error")]}
        (field {:label         "Title"
                :value         (or title "")
                ;:valid?        (session/valid-email? email)
                ;:error-message "Must be an email address"
                :autoComplete  "off"
                :onKeyDown     submit!
                :onChange      #(m/set-string! this :post/title :event %)})
        (field {:label         "Body"
                ;:type          "password"
                ;:value         (or password "")
                ;:valid?        (session/valid-password? password)
                ;:error-message "Password must be at least 8 characters."
                :onKeyDown     submit!
                :autoComplete  "off"
                :onChange      #(m/set-string! this :post/body :event %)})
        (dom/button :.ui.primary.button {:onClick #(submit! true)}
          "Create")))))

(defsc PostsPage [this {:post-list/keys [id label posts] :as props}]
  {:query [:post-list/id :post-list/label {:post-list/posts (comp/get-query Post)}]
   :ident :post-list/id
   :route-segment ["post-list" :post-list/id]
   ;:will-enter (fn [_ {:post-list/keys [id]}] (dr/route-immediate [:post-list/id (keyword id)]))}
   :will-enter (fn [app {:post-list/keys [id]}]
                 (let [id (keyword id)]
                   (dr/route-deferred [:post-list/id id]
                      #(df/load app [:post-list/id id] PostsPage
                                {:post-mutation `dr/target-ready
                                 :post-mutation-params {:target [:post-list/id id]}}))))}
  (div :.ui.container.segment
    (h1 label)
    (when posts
      (map ui-post posts))))