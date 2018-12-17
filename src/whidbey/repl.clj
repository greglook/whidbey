(ns whidbey.repl
  (:require
    [puget.dispatch :as dispatch]
    [puget.printer :as puget]
    [whidbey.types :as types]))


(def options
  "Rendering options."
  {:print-color true
   :namespace-maps true
   :seq-limit 100
   :extend-notation true
   :escape-types #{'clj_http.headers.HeaderMap
                   'datomic.btset.BTSet
                   'datomic.db.Db}})



;; ## Value Rendering

(defn- print-handlers
  "Construct a custom print handler lookup function for Whidbey
  pretty-printing."
  [opts]
  (dispatch/chained-lookup
    (fn escape-handlers
      [t]
      (when (and (class? t)
                 (seq (:escape-types opts))
                 (some #{(symbol (.getName ^Class t))}
                       (:escape-types opts)))
        puget/unknown-handler))
    (fn tag-handlers
      [t]
      (when (and t (:extend-notation opts))
        (let [types (merge types/tag-types (:tag-types opts))
              handlers (or (get types t)
                           (get types (symbol (.getName ^Class t))))]
          (when-let [[tag formatter] (first handlers)]
            (puget/tagged-handler tag (if (symbol? formatter)
                                        (resolve formatter)
                                        formatter))))))
    puget/common-handlers))


(defn- print-options
  "Construct a map of print options to pass to Puget."
  [opts]
  (-> options
      (puget/merge-options opts)
      (assoc :print-handlers (print-handlers opts))))


(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  ([value]
   (render-str value nil))
  ([value opts]
   (puget/pprint-str value (print-options opts))))



;; ## Initialization

(defn update-options!
  "Updates the current rendering options by merging in the supplied map."
  [opts]
  (alter-var-root #'options puget/merge-options opts))


(defn install-data-readers!
  "Initializes the default data-readers map to support Whidbey's custom tags."
  []
  (alter-var-root #'default-data-readers merge types/tag-readers))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (update-options! options)
  (when (:extend-notation options)
    (install-data-readers!)))
