(ns app.routing
  (:require [app.application :refer [SPA]]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [clojure.string :as str]
            [pushy.core :as pushy]))

(defonce history (pushy/pushy
                   (fn [p]
                     (let [route-segments (vec (rest (str/split p "/")))]
                       (log/spy :info route-segments)
                       (dr/change-route SPA route-segments)))
                   identity))

(defn start! []
  (pushy/start! history))

(defn route-to!
  [route-string]
  (pushy/set-token! history route-string))
