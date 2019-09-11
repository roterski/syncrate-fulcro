(ns app.database.seeds
  (:require [app.auth.mutations :refer [create-user]]
            [app.posts.mutations :refer [create-post]]
            [app.posts.resolvers :as pr]
            [talltale.core :as tt]
            [clojure.string :as str]
            [app.database.crux :refer [node get-entities]]))

(defn all-accounts []
  (get-entities `{:find [?e]
                  :where [[?e :account/email _]]}))

(defn all-profiles []
  (get-entities `{:find [?e]
                  :where [[?e :profile/name _]]}))

(defn all-posts []
  (get-entities `{:find [?e]
                  :where [[?e :post/title _]]}))

(defn get-account-profiles [account-id]
  (get-entities `{:find [?e]
                  :where [[?e :profile/account ~account-id]]}))

(defn random-text [word-count]
  (->> (-> (tt/text)
           (str/split #" ")
           shuffle)
       (take word-count)
       (str/join " ")))

(defn seed []
  (let [account-count 10
        posts-per-account 10
        account-ids (repeatedly account-count #(create-user node (tt/email) "password"))
        create-post-fn (fn [account-id] (create-post node account-id {:post/title (random-text 4) :post/body (random-text 30)}))
        post-ids (flatten (repeatedly posts-per-account #(doall (map create-post-fn account-ids))))]
    (do
      (println "created" (count account-ids) "accounts")
      (println "created" (count post-ids) "posts"))))

(comment
  (count (all-accounts))
  (count (all-profiles))
  (count (all-posts))
  (seed))
