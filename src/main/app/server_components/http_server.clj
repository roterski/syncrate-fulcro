(ns app.server-components.http-server
  (:require
    [app.server-components.config :refer [config]]
    [app.server-components.middleware :refer [middleware]]
    [mount.core :refer [defstate]]
    [clojure.pprint :refer [pprint]]
    [org.httpkit.server :as http-kit]
    [taoensso.timbre :as log]))

(def env-config
  (some->>
    "PORT"
    System/getenv
    Integer.
    (conj [:port])
    list
    (into {})))

(defstate http-server
  :start
  (let [cfg (merge (::http-kit/config config) env-config)]
    (log/info "Starting HTTP Server with config " (with-out-str (pprint cfg)))
    (http-kit/run-server middleware cfg))
  :stop (http-server))
