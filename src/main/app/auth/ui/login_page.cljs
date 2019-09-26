(ns app.auth.ui.login-page
  (:require
    [app.auth.state-machines :as session]
    [app.auth.ui.session :refer [Session]]
    [app.routing :refer [route-to!]]
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]))

(defsc LoginPage [this {:account/keys [email]
                        :keys [current-session]
                        :ui/keys [error open?] :as props}]
  {:query [:ui/open? :ui/error :account/email
           {[:current-session '_] (comp/get-query Session)}
           [::uism/asmi-id ::session/session]]
   :ident (fn [] [:component/id :login-page])
   :initial-state {:account/email "" :ui/error ""}
   :route-segment ["login"]}
  (let [current-state (uism/get-active-state this ::session/session)
        password (or (comp/get-state this :password) "")
        loading? (= :state/checking-session current-state)]
    (dom/div
      (dom/h3 :.ui.header "Login")
      (div :.ui.form {:classes [(when (seq error) "error")]}
        (field {:label    "Email"
                :value    email
                :onChange #(m/set-string! this :account/email :event %)})
        (field {:label    "Password"
                :type     "password"
                :value    password
                :onChange #(comp/set-state! this {:password (evt/target-value %)})})
        (div :.ui.error.message error)
        (div :.ui.field
          (dom/button :.ui.button
            {
             :onClick (fn [] (uism/trigger! this ::session/session :event/login {:username email
                                                                                 :password password}))
             :classes [(when loading? "loading")]}
            "Login"))
        (div :.ui.message
          (dom/p "Don't have an account?")
          (dom/a {:onClick (fn []
                             ;(uism/trigger! this ::session/session :event/toggle-modal {})
                             (route-to! "/signup"))}
            "Please sign up!"))))))

(def ui-login (comp/factory LoginPage))
