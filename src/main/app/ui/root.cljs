(ns app.ui.root
  (:require
    [app.model.session :as session]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [clojure.string :as str]))

(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
    (div :.ui.field
      (dom/label {:htmlFor label} label)
      (dom/input input-props)
      (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
        error-message))))

(defsc SignupSuccess [this props]
  {:query         ['*]
   :initial-state {}
   :ident         (fn [] [:component/id :signup-success])
   :route-segment ["signup-success"]
   :will-enter    (fn [app _] (dr/route-immediate [:component/id :signup-success]))}
  (div
    (dom/h3 "Signup Complete!")
    (dom/p "You can now log in!")))

(defsc Signup [this {:account/keys [email password password-again] :as props}]
  {:query             [:account/email :account/password :account/password-again fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config Signup
                          {:account/email          ""
                           :account/password       ""
                           :account/password-again ""}))
   :form-fields       #{:account/email :account/password :account/password-again}
   :ident             (fn [] session/signup-ident)
   :route-segment     ["signup"]
   :componentDidMount (fn [this]
                        (comp/transact! this [(session/clear-signup-form)]))
   :will-enter        (fn [app _] (dr/route-immediate [:component/id :signup]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(session/signup! {:email email :password password})])
                     (log/info "Sign up")))
        checked? (log/spy :info (fs/checked? props))]
    (div
      (dom/h3 "Signup")
      (div :.ui.form {:classes [(when checked? "error")]}
        (field {:label         "Email"
                :value         (or email "")
                :valid?        (session/valid-email? email)
                :error-message "Must be an email address"
                :autoComplete  "off"
                :onKeyDown     submit!
                :onChange      #(m/set-string! this :account/email :event %)})
        (field {:label         "Password"
                :type          "password"
                :value         (or password "")
                :valid?        (session/valid-password? password)
                :error-message "Password must be at least 8 characters."
                :onKeyDown     submit!
                :autoComplete  "off"
                :onChange      #(m/set-string! this :account/password :event %)})
        (field {:label         "Repeat Password" :type "password" :value (or password-again "")
                :autoComplete  "off"
                :valid?        (= password password-again)
                :error-message "Passwords do not match."
                :onChange      #(m/set-string! this :account/password-again :event %)})
        (dom/button :.ui.primary.button {:onClick #(submit! true)}
          "Sign Up")))))

(declare Session)

(defsc Login [this {:account/keys [email]
                    :root/keys    [current-session]
                    :ui/keys      [error open?] :as props}]
  {:query         [:ui/open? :ui/error :account/email
                   {[:root/current-session '_] (comp/get-query Session)}
                   [::uism/asm-id ::session/session]]
   :css           [[:.floating-menu {:position "absolute !important"
                                     :z-index  1000
                                     :width    "300px"
                                     :right    "0px"
                                     :top      "50px"}]]
   :initial-state {:account/email "" :ui/error ""}
   :ident         (fn [] [:component/id :login])}
  (let [current-state (uism/get-active-state this ::session/session)
        {current-user :account/name} current-session
        initial?      (= :initial current-state)
        loading?      (= :state/checking-session current-state)
        logged-in?    (= :state/logged-in current-state)
        {:keys [floating-menu]} (css/get-classnames Login)
        password      (or (comp/get-state this :password) "")] ; c.l. state for security
    (dom/div
      (when-not initial?
        (dom/div :.right.menu
          (if logged-in?
            (dom/button :.item
              {:onClick #(uism/trigger! this ::session/session :event/logout)}
              (dom/span current-user) ent/nbsp "Log out")
            (dom/div :.item {:style   {:position "relative"}
                             :onClick #(uism/trigger! this ::session/session :event/toggle-modal)}
              "Login"
              (when open?
                (dom/div :.four.wide.ui.raised.teal.segment {:onClick (fn [e]
                                                                        ;; Stop bubbling (would trigger the menu toggle)
                                                                        (evt/stop-propagation! e))
                                                             :classes [floating-menu]}
                  (dom/h3 :.ui.header "Login")
                  (div :.ui.form {:classes [(when (seq error) "error")]}
                    (field {:label    "Email"
                            :value    email
                            :onChange #(m/set-string! this :account/email :event %)})
                    (field {:label    "Password"
                            :type     "password"
                            :value    password
                            :onChange #(comp/set-state! this {:password (evt/target-value %)})})
                    (div :.ui.error.message error)
                    (div :.ui.field
                      (dom/button :.ui.button
                        {:onClick (fn [] (uism/trigger! this ::session/session :event/login {:username email
                                                                                             :password password}))
                         :classes [(when loading? "loading")]} "Login"))
                    (div :.ui.message
                      (dom/p "Don't have an account?")
                      (dom/a {:onClick (fn []
                                         (uism/trigger! this ::session/session :event/toggle-modal {})
                                         (dr/change-route this ["signup"]))}
                        "Please sign up!"))))))))))))

(def ui-login (comp/factory Login))

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

(defsc Post [this {:post/keys [title body] :as props}]
  {:query [:post/id :post/title :post/body]
   :ident (fn [] [:post/id (:post/id props)])
   :initial-state (fn [{:keys [id title body] :as params}] {:post/id id :post/title title :post/body body})}
  (dom/li
    (dom/h5 title)
    (dom/p body)))

(def ui-post (comp/factory Post {:keyfn :post/id}))

(defsc PostList [this {:list/keys [id label posts] :as props}]
  {:query [:list/id :list/label {:list/posts (comp/get-query Post)}]
   :ident (fn [] [:list/id (:list/id props)])
   :route-segment ["posts"]
   :will-enter (fn [_ _] (dr/route-immediate [:component/id :posts]))
   :initial-state (fn [{:keys [id label]}]
                    {:list/id id
                     :list/label label
                     :list/posts [(comp/get-initial-state Post {:id 1 :title "Hello" :body "world!"})
                                  (comp/get-initial-state Post {:id 2 :title "Lorem" :body "ipsum"})]})}
  (div :.ui.container.segment
    (h3 "Posts")
    (dom/ul
      (let [posts [(comp/get-initial-state Post {:id 1 :title "Hello" :body "world!"})
                   (comp/get-initial-state Post {:id 2 :title "Lorem" :body "ipsum"})]]
        (map ui-post posts)))))

(def ui-post-list (comp/factory PostList))

;(defsc PostsPage [this props]
;  {}
;  (dom/div
;    (ui-post-list)))


(dr/defrouter TopRouter [this props]
  {:router-targets [Main Signup SignupSuccess Settings PostList]})

(def ui-top-router (comp/factory TopRouter))

(defsc Session
  "Session representation. Used primarily for server queries. On-screen representation happens in Login component."
  [this {:keys [:session/valid? :account/name] :as props}]
  {:query         [:session/valid? :account/name]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge {:session/valid? false :account/name ""}
                      data-tree))
   :initial-state {:session/valid? false :account/name ""}})

(def ui-session (prim/factory Session))

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
        (dom/a :.item {:classes [(when (= :posts current-tab) "active")]
                       :onClick (fn [] (dr/change-route this ["posts"]))} "Posts")
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
