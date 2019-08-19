(ns app.model.database
  (:require [crux.api :as crux]
            [crux.kafka.embedded :as ek]
            [mount.core :refer [defstate]])
  (:import (crux.api ICruxAPI)))

(defstate ^crux.api.ICruxAPI node
  :start (crux/start-standalone-node {:kv-backend "crux.kv.rocksdb.RocksKv"
                                      :db-dir "data/db-dir-1"
                                      :event-log-dir "data/eventlog-1"})
  :stop (apply (:close-fn node) []))

(defn get-entity [id]
  (crux/entity (crux/db node) id))

(defn get-entities [query]
  (->> query
       (crux/q (crux/db node))
       vec
       flatten
       (map get-entity)))
