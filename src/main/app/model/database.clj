(ns app.model.database
  (:require [crux.api :as crux]
            [crux.kafka.embedded :as ek]
            [mount.core :refer [defstate]])
  (:import (crux.api ICruxAPI)))

; (def ^crux.api.ICruxAPI node
;  (crux/start-standalone-node {:kv-backend "crux.kv.memdb.MemKv"
;                               :db-dir "data/db-dir-1"
;                               :event-log-dir "data/eventlog-1"}))
;(def ^crux.api.ICruxAPI node
;  (crux/start-standalone-node {:kv-backend "crux.kv.rocksdb.RocksKv"
;                               :db-dir "data/db-dir-1"
;                               :event-log-dir "data/eventlog-1"}))

(def storage-dir "data")
(def embedded-kafka-options
  {:crux.kafka.embedded/zookeeper-data-dir (str storage-dir "/zookeeper")
   :crux.kafka.embedded/kafka-log-dir (str storage-dir "/kafka-log")
   :crux.kafka.embedded/kafka-port 9093})

(defstate embedded-kafka :start (ek/start-embedded-kafka embedded-kafka-options))

(defn new-database []
  (crux/start-cluster-node
   {:kv-backend "crux.kv.memdb.MemKv"
    :bootstrap-servers (get-in embedded-kafka [:options :bootstrap-servers])}))

(defstate ^crux.api.ICruxAPI node :start (new-database))

(defn get-entity [id]
  (crux/entity (crux/db node) id))

(defn get-entities [query]
  (->> query
       (crux/q (crux/db node))
       vec
       flatten
       (map get-entity)))
