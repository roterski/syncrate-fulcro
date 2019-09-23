(ns app.client
  (:require
    [app.application :refer [SPA]]
    [app.routing :as routing]
    [com.fulcrologic.fulcro.application :as app]
    [app.ui.root :as root]
    [app.auth.ui.login-page :refer [LoginPage]]
    [app.auth.ui.session :refer [Session]]
    [app.auth.state-machines :as session]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro-css.css-injection :as cssi]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]))

(defmutation finish-login [_]
  (action [{:keys [app state]}]
    (swap! state (fn [s] (assoc-in s [:component/id :top-chrome :root/ready?] true)))))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (cssi/upsert-css "componentcss" {:component root/Root})
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/info "Application starting.")
  (cssi/upsert-css "componentcss" {:component root/Root})
  (app/set-root! SPA root/Root {:initial-state? true})
  (app/mount! SPA root/Root "app" {:initial-state? true})
  (dr/initialize! SPA)
  (routing/start!)
  (log/info "Starting session machine.")
  (df/load! SPA :current-session Session {:post-mutation `finish-login})
  (uism/begin! SPA session/session-machine ::session/session
    {:actor/login-form      LoginPage
     :actor/current-session Session}))


(comment
  (inspect/app-started! SPA)
  (app/mounted? SPA)
  (app/set-root! SPA root/Root {:initialize-state? true})
  (uism/begin! SPA session/session-machine ::session/session
               {:actor/login-form      root/Login
                :actor/current-session root/Session})

  (reset! (::app/state-atom SPA) {})

  (merge/merge-component! my-app Settings {:account/time-zone "America/Los_Angeles"
                                           :account/real-name "Joe Schmoe"})
  (dr/initialize! SPA)
  (app/current-state SPA)
  (dr/change-route SPA ["settings"])
  (app/mount! SPA root/Root "app")
  (comp/get-query root/Root {})
  (comp/get-query root/Root (app/current-state SPA))

  (-> SPA ::app/runtime-atom deref ::app/indexes)
  (comp/class->any SPA root/Root)
  (let [s (app/current-state SPA)]
    (fdn/db->tree [{[:component/id :login] [:ui/open? :ui/error :account/email
                                            {[:root/current-session '_] (comp/get-query root/Session)}
                                            [::uism/asm-id ::session/session]]}] {} s)))
