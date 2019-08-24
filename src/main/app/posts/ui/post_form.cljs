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
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defsc PostForm [this {:post/keys [id title body] :as props}]
  {:query             [:post/id :post/title :post/body fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config PostForm
                          {:post/title ""
                           :post/body  ""}))
   :form-fields       #{:post/title :post/body}
   :ident             (fn [] post-form-ident)
   :componentDidMount (fn [this]
                        (comp/transact! this [(post/clear-post-form)]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(post/create-post! {:title title :body body})])
                     (log/info "Create post")))
        checked? (log/spy :info (fs/checked? props))]
    (div :.ui.form {:classes [(when checked? "error")]}
      (field {:label         "Title"
              :value         (or title "")
              ;:valid?        (session/valid-email? email)
              ;:error-message "Must be an email address"
              :autoComplete  "off"
              :onKeyDown     submit!
              :onChange      #(m/set-string! this :post/title :event %)})
      (field {:label         "Body"
              :onKeyDown     submit!
              :autoComplete  "off"
              :onChange      #(m/set-string! this :post/body :event %)})
      (dom/button :.ui.primary.button {:onClick #(submit! true)}
        "Create"))))

(def ui-post-form (comp/factory PostForm))
