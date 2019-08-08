(ns app.auth.helpers
  (:require
    [com.fulcrologic.fulcro.components :as comp]
    [clojure.string :as str]))

(def signup-ident [:component/id :signup])
(defn signup-class [] (comp/registry-key->class :app.auth.ui.signup/Signup))

(defn valid-email? [email] (str/includes? email "@"))
(defn valid-password? [password] (> (count password) 7))
