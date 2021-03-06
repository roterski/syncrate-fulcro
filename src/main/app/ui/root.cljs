(ns app.ui.root
  (:require
    [app.auth.ui.login-button :refer [ui-login-button]]
    [app.auth.ui.login-page :refer [LoginPage]]
    [app.auth.ui.session :refer [Session]]
    [app.auth.ui.signup :refer [Signup]]
    [app.auth.ui.signup-success :refer [SignupSuccess]]
    [app.posts.ui.post-form :refer [PostForm]]
    [app.posts.ui.new-post-page :refer [NewPostPage]]
    [app.posts.ui.post-list-page :refer [PostListPage]]
    [app.posts.ui.post-show-page :refer [PostShowPage]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h1 h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.components :as comp]))

(defsc Main [this {:main/keys [welcome-message]}]
  {:query         [:main/welcome-message]
   :initial-state {:main/welcome-message "Hello world!"}
   :ident         (fn [] [:component/id :main])
   :route-segment ["main"]}
  (div :.ui.container.segment
    (h3 "Main")
    (p welcome-message)))

(defsc Settings [this {:keys [:account/time-zone :account/real-name] :as props}]
  {:query         [:account/time-zone :account/real-name]
   :ident         (fn [] [:component/id :settings])
   :route-segment ["settings"]
   :initial-state {}}
  (div :.ui.container.segment
    (h3 "Settings")))

(dr/defrouter TopRouter [this props]
  {:router-targets [Main Settings LoginPage Signup SignupSuccess NewPostPage PostListPage PostShowPage]})

(def ui-top-router (comp/factory TopRouter))


(defsc TopChrome [this {:root/keys [ready? router current-session]}]
  {:query         [:root/ready?
                   {:root/router (comp/get-query TopRouter)}
                   {:root/current-session (comp/get-query Session)}
                   [::uism/asm-id ::TopRouter]]
   :ident         (fn [] [:component/id :top-chrome])
   :initial-state {:root/ready? false
                   :root/router          {}
                   :root/current-session {}}}
  (let [current-tab (some-> (dr/current-route this this) first keyword)]
    (div :.ui.container
      (div :.ui.secondary.pointing.menu
        (dom/a :.item {:classes [(when (= :main current-tab) "active")]
                       :href "/main"} "Main")
        (when (:session/valid? current-session)
          (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
                         :href "/settings"} "Settings"))
        (when (:session/valid? current-session)
          (dom/a :.item {:classes [(when (= :new-post current-tab) "active")]
                         :href "/new-post"} "New Post"))
        (dom/a :.item {:classes [(when (= :post-list current-tab) "active")]
                       :href "/posts/all/page/1"} "Posts")
        (div :.right.menu
          (ui-login-button this current-session)))
      (div :.ui.grid
        (div :.ui.row
          (when ready?
           (ui-top-router router)))))))

(def ui-top-chrome (comp/factory TopChrome))

(defsc Root [this {:root/keys [top-chrome]}]
  {:query             [{:root/top-chrome (comp/get-query TopChrome)}]
   :ident             (fn [] [:component/id :ROOT])
   :initial-state     {:root/top-chrome {}}}
  (ui-top-chrome top-chrome))
