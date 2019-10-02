(ns app.database.crux
  (:require [crux.api :as crux]
            [mount.core :refer [defstate]]
            [app.server-components.config :refer [config]])
  (:import (crux.api ICruxAPI)))

(defn jdbc-config-from-url [url]
  (let [regex #"postgres://(?<user>.*):(?<password>.*)@(?<host>.*):(?<port>\d+)\/(?<database>.*)"
        matcher (re-matcher regex url)]
    (if (.matches matcher)
      {:dbtype   "postgresql"
       :dbname   (.group matcher "database")
       :user     (.group matcher "user")
       :password (.group matcher "password")
       :host     (.group matcher "host")
       :port     (.group matcher "port")}
      (throw (ex-info (str "Can't match jdbc config from url") {:url url})))))

(defn jdbc-config [cfg]
  (let [{:keys [url dbname user password]} (:db/postgres cfg)]
    (if url
      (jdbc-config-from-url url)
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
