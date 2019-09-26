(ns app.comments.ui.comment-form
  (:require
    [app.comments.validations]
    [app.ui.components :refer [field]]
    [app.auth.ui.session :refer [Session]]
    [goog.object :as gobj]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.tempid :refer [tempid]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(defn add-comment*
  [state-map {:comment/keys [id post-id parent-id] :as props}]
  (let [comment-ident [:comment/id id]
        parent-ident (if (nil? parent-id)
                       [:post/id post-id :post/new-comment]
                       [:comment/id parent-id :comment/new-comment])]
    (-> state-map
        (assoc-in parent-ident comment-ident)
        (assoc-in comment-ident props))))

(declare CommentForm)

(defmutation add-comment-form
  [props]
  (action [{:keys [state]}]
    (let [comment-id (tempid)]
      (swap! state (fn [s]
                     (-> s
                         (add-comment* (merge {:comment/id comment-id :comment/body ""} props))
                         (fs/add-form-config* CommentForm [:comment/id comment-id])))))))

(defn remove-comment*
  [state-map {:comment/keys [id post-id parent-id] :as props}]
  (cond-> state-map
    true (update-in [:comment/id] dissoc id)
    (nil? parent-id) (update-in [:post/id post-id] dissoc :post/new-comment)
    (some? parent-id) (update-in [:comment/id parent-id] dissoc :comment/new-comment)))

(defn remove-comment-form*
  [state-map {:comment/keys [id]}]
  (let [form-id {:table :comment/id :row id}]
    (-> state-map
      (update-in [::fs/forms-by-ident] dissoc form-id))))

(defmutation remove-comment
  [props]
  (action [{:keys [state]}]
    (swap! state (fn [s]
                   (-> s
                       (remove-comment* props)
                       (remove-comment-form* props))))))

(defn move-comment-from-new*
  [state-map {:comment/keys [id post-id parent-id] :as props}]
  (let [comment-ident [:comment/id id]
        post-comment? (nil? parent-id)]
    (cond-> state-map
            post-comment? (update-in [:post/id post-id] dissoc :post/new-comment)
            post-comment? (update-in [:post/id post-id :post/comments] (fnil conj []) comment-ident)
            (not post-comment?) (update-in [:comment/id parent-id] dissoc :comment/new-comment)
            (not post-comment?) (update-in [:comment/id parent-id :comment/children] (fnil conj []) comment-ident))))

(defmutation create-comment! [{:comment/keys [tempid] :as props}]
  (action [{:keys [state]}]
    (log/info "Creating comment..."))
  (ok-action [{:keys [state result] :as env}]
    (log/info "...comment created successfully")
    (let [id (get-in result [:body `create-comment! :tempids tempid])]
      (swap! state (fn [s]
                     (-> s
                         (move-comment-from-new* (merge props {:comment/id id}))
                         (remove-comment-form* {:comment/id id}))))))
  (error-action [env]
    (log/error "...creating comment failed")
    (log/error env))
  (remote [{:keys [state] :as env}] true))

(defsc CommentForm [this {:comment/keys [id body] :as props} {:keys [post-id parent-id]}]
  {:query             [:comment/id :comment/body fs/form-config-join]
   :form-fields       #{:comment/body}
   :ident             :comment/id
   :initLocalState (fn [this _]
                     {:save-ref (fn [r] (gobj/set this "input-ref" r))})
   :componentDidMount (fn [this]
                        (when-let [input-field (gobj/get this "input-ref")]
                          (.focus input-field)))}
  (let [validity (fs/get-spec-validity props :comment/body)
        submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this `[(fs/mark-complete! {:field :comment/body})])
                     (when (contains? #{:valid} validity)
                       (comp/transact! this `[(create-comment! {:comment/tempid ~id :comment/body ~body :comment/post-id ~post-id :comment/parent-id ~parent-id})]))))
        cancel  #(comp/transact! this `[(remove-comment {:comment/id ~id :comment/post-id ~post-id :comment/parent-id ~parent-id})])]
    (div
      (div :.ui.form {:classes [(when (contains? #{:invalid} (fs/get-spec-validity props)) "error")]}
        (field {:label         "New Comment"
                :value         (or body "")
                :valid?        (contains? #{:valid :unchecked} validity)
                :error-message "Cannot be blank"
                :ref           (comp/get-state this :save-ref)
                :onKeyDown     submit!
                :autoComplete  "off"
                :onChange      #(do
                                   (comp/transact! this `[(fs/mark-complete! {:field :comment/body})])
                                   (m/set-string! this :comment/body :event %))})
        (dom/button :.ui.primary.button {:onClick #(submit! true) :disabled (contains? #{:invalid :unchecked} validity)}
          "Create")
        (dom/button :.ui.secondary.button {:onClick cancel} "Cancel")))))

(def ui-comment-form (comp/computed-factory CommentForm {:keyfn #(-> % :comment/id str)}))
