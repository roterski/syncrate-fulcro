(ns app.auth.mutations
  (:require
    [app.util :as util]
    [crux.api :as crux]
    [buddy.hashers :as hashers]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]))

(defn find-user [db email]
  (crux/q db
    `{:find [e]
      :where [[e :account/email ~email]]}))

(defn get-user [db email]
  (crux/entity db (first (first (find-user db email)))))

(defn create-user [crux-node email password]
  (let [password-hash (hashers/derive password)]
    (crux/submit-tx
      crux-node
      [[:crux.tx/put
        {:crux.db/id (util/uuid)
         :account/email email
         :account/password-hash password-hash}]])))

(defn response-updating-session
  "Uses `mutation-response` as the actual return value for a mutation, but also stores the data into the (cookie-based) session."
  [mutation-env mutation-response]
  (let [existing-session (some-> mutation-env :ring/request :session)]
    (fmw/augment-response
      mutation-response
      (fn [resp]
        (let [new-session (merge existing-session mutation-response)]
          (assoc resp :session new-session))))))

(defmutation login [{:keys [db] :as env} {:keys [username password]}]
  {::pc/output [:session/valid? :account/name]}
  (log/info "Authenticating" username)
  (let [user (get-user db username)
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

(defmutation signup! [{:keys [db crux-node]} {:keys [email password]}]
  {::pc/output [:signup/result]}
  (if (empty? (find-user db email))
    (do
      (create-user crux-node email password)
      {:signup/result "OK"})
    (throw (ex-info "Email is taken" {:email email}))))
