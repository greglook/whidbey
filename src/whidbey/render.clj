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
     :seq-limit 100
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
    (fn [t]
      (when (and (class? t)
                 (seq (:escape-types printer))
                 (some #{(symbol (.getName ^Class t))}
                       (:escape-types printer)))
        puget/pr-handler))
    (fn [t]
      (when-let [custom-lookup (:print-handlers printer)]
        (custom-lookup t)))
    (fn [t]
      (when (:extend-notation printer)
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
