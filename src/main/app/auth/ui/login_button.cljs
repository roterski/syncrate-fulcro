(ns app.auth.ui.login-button
  (:require [app.auth.ui.session :refer [Session]]
            [app.auth.state-machines :as session]
            [app.routing :refer [route-to!]]
            [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
            [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
            [com.fulcrologic.fulcro-css.css :as css]
            [com.fulcrologic.fulcro.dom.html-entities :as ent]
            [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]))

(defn ui-login-button [this {:keys [account/name session/valid?]}]
  (let [logged-in?    valid?]
    (dom/div
        (dom/div :.right.menu
          (if logged-in?
            (dom/button :.item
              {:onClick #(uism/trigger! this ::session/session :event/logout)}
              (dom/span name) ent/nbsp "Log out")
            (dom/button :.item
                        {:onClick #(route-to! "/login")} "Log in"))))))
