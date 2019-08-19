(ns app.auth.resolvers
  (:require
    [app.auth.mutations :refer [login logout signup!]]
    [app.database.crux :refer [node]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]))

(defresolver current-session-resolver [env input]
  {::pc/output [{::current-session [:session/valid? :account/name]}]}
  (let [{:keys [account/name session/valid?]} (get-in env [:ring/request :session])]
    (if valid?
      (do
        (log/info name "already logged in!")
        {::current-session {:session/valid? true :account/name name}})
      {::current-session {:session/valid? false}})))

(def resolvers [current-session-resolver login logout signup!])
