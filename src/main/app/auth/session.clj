(ns app.auth.session
  (:require
    [app.model.database :refer [node]]
    [app.util :as util]
    [crux.api :as crux]
    [buddy.hashers :as hashers]
    [datascript.core :as d]
    ;[ghostwheel.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]))

(defonce account-database (atom {}))

(defn find-user [email]
  (crux/q (crux/db node)
    `{:find [e]
      :where [[e :account/email ~email]]}))

(defn get-user [email]
  (crux/entity (crux/db node) (first (first (find-user email)))))

(defn create-user [email password]
  (let [password-hash (hashers/derive password)]
    (crux/submit-tx
      node
      [[:crux.tx/put
        {:crux.db/id (keyword "account.id" (str (util/uuid)))
         :account/email email
         :account/password-hash password-hash}]])))

(defresolver current-session-resolver [env input]
  {::pc/output [{::current-session [:session/valid? :account/name]}]}
  (let [{:keys [account/name session/valid?]} (get-in env [:ring/request :session])]
    (if valid?
      (do
        (log/info name "already logged in!")
        {::current-session {:session/valid? true :account/name name}})
      {::current-session {:session/valid? false}})))

(defn response-updating-session
  "Uses `mutation-response` as the actual return value for a mutation, but also stores the data into the (cookie-based) session."
  [mutation-env mutation-response]
  (let [existing-session (some-> mutation-env :ring/request :session)]
    (fmw/augment-response
      mutation-response
      (fn [resp]
        (let [new-session (merge existing-session mutation-response)]
          (assoc resp :session new-session))))))

(defmutation login [env {:keys [username password]}]
  {::pc/output [:session/valid? :account/name]}
  (log/info "Authenticating" username)
  (let [user (get-user username)
        password-hash (:account/password-hash user)
        credentials-valid? (hashers/check password password-hash)]
    (if credentials-valid?
      (response-updating-session env
       {:session/valid? true
        :account/id     (:crux.db/id user)
        :account/name   username})
      (do
        (log/error "Invalid credentials supplied for" username)
        (throw (ex-info "Invalid credentials" {:username username}))))))

(defmutation logout [env params]
  {::pc/output [:session/valid?]}
  (response-updating-session env {:session/valid? false :account/name ""}))

(defmutation signup! [env {:keys [email password]}]
  {::pc/output [:signup/result]}
  (if (empty? (find-user email))
    (do
      (create-user email password)
      {:signup/result "OK"})
    (throw (ex-info "Email is taken" {:email email}))))

(def resolvers [current-session-resolver login logout signup!])
