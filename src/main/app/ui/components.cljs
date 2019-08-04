(ns app.ui.components
  (:require
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]))

(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
    (dom/div :.ui.field
         (dom/label {:htmlFor label} label)
         (dom/input input-props)
         (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
                     error-message))))

(defsc PlaceholderImage
  "Generates an SVG image placeholder of the given size and with the given label
  (defaults to showing 'w x h'.

  ```
  (ui-placeholder {:w 50 :h 50 :label \"avatar\"})
  ```
  "
  [this {:keys [w h label]}]
  (let [label (or label (str w "x" h))]
    (dom/svg #js {:width w :height h}
      (dom/rect #js {:width w :height h :style #js {:fill        "rgb(200,200,200)"
                                                    :strokeWidth 2
                                                    :stroke      "black"}})
      (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label))))

(def ui-placeholder (prim/factory PlaceholderImage))
