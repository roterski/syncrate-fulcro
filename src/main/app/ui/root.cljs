(ns app.ui.root
  (:require
    [app.auth.session :as session]
    [app.auth.ui :refer [Signup SignupSuccess Login Session ui-login]]
    [app.posts.model :as post]
    [app.posts.ui :refer [PostForm PostsPage]]
    [app.ui.components :refer [field]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [clojure.string :as str]))


(defsc Main [this {:main/keys [welcome-message]}]
  {:query         [:main/welcome-message]
   :initial-state {:main/welcome-message "Hello world!"}
   :ident         (fn [] [:component/id :main])
   :route-segment ["main"]
   :will-enter    (fn [_ _] (dr/route-immediate [:component/id :main]))}
  (div :.ui.container.segment
    (h3 "Main")
    (p welcome-message)))

(defsc Settings [this {:keys [:account/time-zone :account/real-name] :as props}]
  {:query         [:account/time-zone :account/real-name]
   :ident         (fn [] [:component/id :settings])
   :route-segment ["settings"]
   :will-enter    (fn [_ _] (dr/route-immediate [:component/id :settings]))
   :initial-state {}}
  (div :.ui.container.segment
    (h3 "Settings")))

(dr/defrouter TopRouter [this props]
  {:router-targets [Main Signup SignupSuccess PostForm Settings PostsPage]})

(def ui-top-router (comp/factory TopRouter))


(defsc TopChrome [this {:root/keys [router current-session login]}]
  {:query         [{:root/router (comp/get-query TopRouter)}
                   {:root/current-session (comp/get-query Session)}
                   [::uism/asm-id ::TopRouter]
                   {:root/login (comp/get-query Login)}]
   :ident         (fn [] [:component/id :top-chrome])
   :initial-state {:root/router          {}
                   :root/login           {}
                   :root/current-session {}}}
  (let [current-tab (some-> (dr/current-route this this) first keyword)]
    (div :.ui.container
      (div :.ui.secondary.pointing.menu
        (dom/a :.item {:classes [(when (= :main current-tab) "active")]
                       :onClick (fn [] (dr/change-route this ["main"]))} "Main")
        (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
                       :onClick (fn [] (dr/change-route this ["settings"]))} "Settings")
        (dom/a :.item {:classes [(when (= :new-post current-tab) "active")]
                       :onClick (fn [] (dr/change-route this ["new-post"]))} "New Post")
        (dom/a :.item {:classes [(when (= :post-list current-tab) "active")]
                       :onClick (fn [] (dr/change-route this ["post-list" "all-posts"]))} "Posts")
        (div :.right.menu
          (ui-login login)))
      (div :.ui.grid
        (div :.ui.row
          (ui-top-router router))))))

(def ui-top-chrome (comp/factory TopChrome))

(defsc Root [this {:root/keys [top-chrome]}]
  {:query             [{:root/top-chrome (comp/get-query TopChrome)}]
   :ident             (fn [] [:component/id :ROOT])
   :initial-state     {:root/top-chrome {}}}
  (ui-top-chrome top-chrome))
