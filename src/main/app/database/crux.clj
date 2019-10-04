(ns app.database.crux
  (:require [crux.api :as crux]
            [mount.core :refer [defstate]]
            [app.server-components.config :refer [config]]
            [taoensso.timbre :as log])
  (:import (crux.api ICruxAPI)))

(defn jdbc-config-from-url [url]
  (let [regex #"postgres://(?<user>.*):(?<password>.*)@(?<host>.*):(?<port>\d+)\/(?<database>.*)"
        matcher (re-matcher regex url)]
    (log/info "Configuring jdbc connection with DATABASE_URL env var")
    (if (.matches matcher)
      {:dbtype   "postgresql"
       :dbname   (.group matcher "database")
       :user     (.group matcher "user")
       :password (.group matcher "password")
       :host     (.group matcher "host")
       :port     (.group matcher "port")}
      (throw (ex-info (str "Can't match jdbc config with DATABASE_URL env var") {:url url})))))

(defn jdbc-config [cfg]
  (if-let [url (System/getenv "DATABASE_URL")]
    (jdbc-config-from-url url)
    (let [{:keys [dbname user password]} (:db/postgres cfg)]
      (log/info "Configuring jdbc connection with config file")
      {:dbtype "postgresql"
       :dbname dbname
       :user user
       :password password})))

(defstate ^crux.api.ICruxAPI node
  :start (crux/start-jdbc-node (jdbc-config config))
  :stop (apply (:close-fn node) []))

(defn get-entity [id]
  (crux/entity (crux/db node) id))

(defn get-entities [query]
  (->> query
       (crux/q (crux/db node))
       vec
       flatten
       (map get-entity)))
