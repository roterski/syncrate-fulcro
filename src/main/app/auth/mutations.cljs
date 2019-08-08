(ns app.auth.mutations
  (:require
    [app.application :refer [SPA]]
    [app.auth.helpers :refer [signup-class signup-ident valid-email? valid-password?]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defn clear-signup-form*
  "Mutation helper: Updates state map with a cleared signup form that is configured for form state support."
  [state-map]
  (-> state-map
    (assoc-in signup-ident
      {:account/email          ""
       :account/password       ""
       :account/password-again ""})
    (fs/add-form-config* (signup-class) signup-ident)))

(defmutation clear-signup-form [_]
  (action [{:keys [state]}]
    (swap! state clear-signup-form*)))

(defmutation signup! [_]
  (action [{:keys [state]}]
    (log/info "Marking complete")
    (swap! state fs/mark-complete* signup-ident))
  (ok-action [{:keys [app state]}]
    (dr/change-route app ["signup-success"]))
  (remote [{:keys [state] :as env}]
    (let [{:account/keys [email password password-again]} (get-in @state signup-ident)]
      (boolean (and (valid-email? email) (valid-password? password)
                 (= password password-again))))))
