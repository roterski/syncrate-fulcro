(ns app.auth.state-machines
  (:require
    [app.application :refer [SPA]]
    [app.routing :refer [route-to!]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.mutations :as m]))

(defn clear [env]
  (uism/assoc-aliased env :error ""))

(defn logout [env]
  (-> env
    (clear)
    (uism/assoc-aliased :username "" :session-valid? false :current-user "")
    (uism/trigger-remote-mutation :actor/login-form 'app.auth.mutations/logout {})
    (uism/activate :state/logged-out)))

(defn login [{::uism/keys [event-data] :as env}]
  (-> env
    (clear)
    (uism/trigger-remote-mutation :actor/login-form 'app.auth.mutations/login
      {:username          (:username event-data)
       :password          (:password event-data)
       ::m/returning      (uism/actor-class env :actor/current-session)
       ::uism/ok-event    :event/complete
       ::uism/error-event :event/failed})
    (uism/activate :state/checking-session)))

(defn process-session-result [env error-message]
  (let [success? (uism/alias-value env :session-valid?)]
    ;(when success?
    ;  (route-to! "/main"))
    (cond-> (clear env)
      success? (->
                 (uism/assoc-aliased :modal-open? false)
                 (uism/activate :state/logged-in))
      (not success?) (->
                       (uism/assoc-aliased :error error-message)
                       (uism/activate :state/logged-out)))))

(def global-events
  {:event/toggle-modal {::uism/handler (fn [env] (uism/update-aliased env :modal-open? not))}})

(uism/defstatemachine session-machine
  {::uism/actors
   #{:actor/login-form :actor/current-session}

   ::uism/aliases
   {:username       [:actor/login-form :account/email]
    :error          [:actor/login-form :ui/error]
    :modal-open?    [:actor/login-form :ui/open?]
    :session-valid? [:actor/current-session :session/valid?]
    :current-user   [:actor/current-session :account/name]}

   ::uism/states
   {:initial
    {::uism/target-states #{:state/logged-in :state/logged-out}
     ::uism/events        {::uism/started  {::uism/handler (fn [env]
                                                             ;(dr/change-route SPA ["main"])
                                                             (-> env
                                                               (uism/assoc-aliased :error "")
                                                               (uism/load ::current-session :actor/current-session
                                                                 {::uism/ok-event    :event/complete
                                                                  ::uism/error-event :event/failed})))}
                           :event/failed   {::uism/target-state :state/logged-out}
                           :event/complete {::uism/target-states #{:state/logged-in :state/logged-out}
                                            ::uism/handler       #(process-session-result % "")}}}

    :state/checking-session
    {::uism/events (merge global-events
                     {:event/failed   {::uism/target-states #{:state/logged-out}
                                       ::uism/handler       (fn [env]
                                                              (-> env
                                                                (clear)
                                                                (uism/assoc-aliased :error "Server error.")))}
                      :event/complete {::uism/target-states #{:state/logged-out :state/logged-in}
                                       ::uism/handler       #(process-session-result % "Invalid Credentials.")}})}

    :state/logged-in
    {::uism/events (merge global-events
                     {:event/logout {::uism/target-states #{:state/logged-out}
                                     ::uism/handler       logout}})}

    :state/logged-out
    {::uism/events (merge global-events
                     {:event/login {::uism/target-states #{:state/checking-session}
                                    ::uism/handler       login}})}}})
