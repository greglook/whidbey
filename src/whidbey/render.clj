(ns whidbey.render
  (:require
    [puget.printer :as puget]))

;; ## Rendering Options

(def options
  "Currently configured Puget options."
  {:print-color true
   :escape-types #{'clj_http.headers.HeaderMap
                   'datomic.btset.BTSet
                   'datomic.db.Db}})


(defn update-options!
  "Updates the current rendering options by merging in the supplied map."
  [opts]
  (alter-var-root #'options puget/merge-options opts))


(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  [value]
  (puget/pprint-str value options))
