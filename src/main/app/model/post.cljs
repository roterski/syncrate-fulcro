(ns app.model.post
  (:require
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [clojure.string :as str]))
;
;(defn clear [env]
;  (uism/assoc-aliased env :error ""))
;
;(defn logout [env]
;  (-> env
;    (clear)
;    (uism/assoc-aliased :username "" :session-valid? false :current-user "")
;    (uism/trigger-remote-mutation :actor/login-form 'app.model.session/logout {})
;    (uism/activate :state/logged-out)))

;(defn login [{::uism/keys [event-data] :as env}]
;  (-> env
;    (clear)
;    (uism/trigger-remote-mutation :actor/login-form 'app.model.session/login
;      {:username          (:username event-data)
;       :password          (:password event-data)
;       ::m/returning      (uism/actor-class env :actor/current-session)
;       ::uism/ok-event    :event/complete
;       ::uism/error-event :event/failed})
;    (uism/activate :state/checking-session)))
;
;(defn process-session-result [env error-message]
;  (let [success? (uism/alias-value env :session-valid?)]
;    (when success?
;      (dr/change-route SPA ["main"]))
;    (cond-> (clear env)
;      success? (->
;                 (uism/assoc-aliased :modal-open? false)
;                 (uism/activate :state/logged-in))
;      (not success?) (->
;                       (uism/assoc-aliased :error error-message)
;                       (uism/activate :state/logged-out)))))
;
;(def global-events
;  {:event/toggle-modal {::uism/handler (fn [env] (uism/update-aliased env :modal-open? not))}})

(def post-form-ident [:component/id :post-form])
(defn post-form-class [] (comp/registry-key->class :app.ui.root/PostForm))

(defn clear-post-form*
  "Mutation helper: Updates state map with a cleared signup form that is configured for form state support."
  [state-map]
  (-> state-map
    (assoc-in post-form-ident
      {:post/title          ""
       :post/body       ""})
    (fs/add-form-config* (post-form-class) post-form-ident)))

(defmutation clear-post-form [_]
  (action [{:keys [state]}]
    (swap! state clear-post-form*)))

;(defn valid-email? [email] (str/includes? email "@"))
;(defn valid-password? [password] (> (count password) 7))
;
(defmutation create-post! [_]
  (action [{:keys [state]}]
    (log/info "Marking complete")
    (swap! state fs/mark-complete* post-form-ident))
  (ok-action [{:keys [app state]}]
    (dr/change-route app ["posts"]))
  (remote [{:keys [state] :as env}] true))
    ;(let [{:post/keys [title body]} (get-in @state post-form-ident)]
    ;  (boolean (and (valid-email? email) (valid-password? password)
    ;             (= password password-again))))))

