(ns whidbey.render
  (:require
    [puget.dispatch :as dispatch]
    [puget.printer :as puget]
    [whidbey.types :as types]))


;; ## Rendering Options

(def printer
  "Currently configured Puget printer record."
  (puget/pretty-printer
    {:print-color true
     :extend-notation true
     :escape-types #{'clj_http.headers.HeaderMap
                     'datomic.btset.BTSet
                     'datomic.db.Db}}))


(defn update-options!
  "Updates the current rendering options by merging in the supplied map."
  [opts]
  (alter-var-root #'options puget/merge-options opts))


(def print-handlers
  "Custom handler lookup for Whidbey's printer."
  (dispatch/chained-lookup
    (dispatch/symbolic-lookup
      (fn [t]
        (when (some #{t} (:escape-types options))
          puget/pr-handler)))
    (fn [t]
      (when-let [custom-lookup (:print-handlers options)]
        (custom-lookup options)))
    (fn [t]
      (when (:extend-notation options)
        (types/tag-handlers t)))
    puget/common-handlers))



;; ## Value Rendering

(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  [value]
  (-> printer
      (assoc :print-handlers print-handlers)
      (puget/render-str value)))
