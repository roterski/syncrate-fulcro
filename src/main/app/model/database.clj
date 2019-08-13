(ns app.model.database
  (:require [crux.api :as crux])
  (:import (crux.api ICruxAPI)))

(def ^crux.api.ICruxAPI node
  (crux/start-standalone-node {:kv-backend "crux.kv.memdb.MemKv"
                               :db-dir "data/db-dir-1"
                               :event-log-dir "data/eventlog-1"}))
;(def ^crux.api.ICruxAPI node
;  (crux/start-standalone-node {:kv-backend "crux.kv.rocksdb.RocksKv"
;                               :db-dir "data/db-dir-1"
;                               :event-log-dir "data/eventlog-1"}))

(defn get-entity [id]
  (crux/entity (crux/db node) id))

(defn get-entities [query]
  (->> query
       (crux/q (crux/db node))
       vec
       flatten
       (map get-entity)))
