(ns whidbey.repl
  (:require
    [clojure.tools.nrepl.middleware.pr-values :refer [pr-values]]
    [puget.dispatch :as dispatch]
    [puget.printer :as puget]
    [whidbey.types :as types]))


;; ## Value Rendering

(def printer
  "Currently configured Puget printer record."
  (puget/pretty-printer
    {:print-color true
     :extend-notation true
     :seq-limit 100
     :escape-types #{'clj_http.headers.HeaderMap
                     'datomic.btset.BTSet
                     'datomic.db.Db}}))


(def print-handlers
  "Custom handler lookup for Whidbey's printer."
  (dispatch/chained-lookup
    (fn escape-handlers [t]
      (when (and (class? t)
                 (seq (:escape-types printer))
                 (some #{(symbol (.getName ^Class t))}
                       (:escape-types printer)))
        puget/unknown-handler))
    (fn tag-handlers [t]
      (when (and t (:extend-notation printer))
        (let [types (merge types/tag-types (:tag-types printer))
              t-sym (symbol (.getName ^Class t))]
          (when-let [[tag formatter] (first (get types t-sym))]
            (puget/tagged-handler tag (if (symbol? formatter)
                                        (resolve formatter)
                                        formatter))))))
    puget/common-handlers))


(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  [value]
  (-> printer
      (assoc :print-handlers print-handlers)
      (puget/render-str value)))



;; ## Option Control

(defn update-options!
  "Updates the current rendering options by merging in the supplied map."
  [opts]
  (alter-var-root #'printer puget/merge-options opts))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (update-options! options)
  (alter-var-root #'pr-values (constantly identity))
  (if (:extend-notation printer)
    (doseq [[tag reader] types/tag-readers]
      (alter-var-root #'default-data-readers assoc tag reader))))
