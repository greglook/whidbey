(ns whidbey.render
  (:require
    [puget.dispatch :as dispatch]
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



;; ## Type Extensions

(defn bin-str
  "Renders a byte array as a base-64 encoded string."
  [bin]
  (apply str (map char (b64/encode bin))))


(defn read-bin
  "Reads a base64-encoded string into a byte array. Suitable as a data-reader
  for `whidbey/bin` literals."
  ^bytes
  [^String bin]
  (b64/decode (.getBytes bin)))


(defn read-uri
  "Constructs a URI from a string value. Suitable as a data-reader for
  `whidbey/uri` literals."
  ^URI
  [^String uri]
  (URI. uri))


(def tag-handlers
  "Extra print-handlers for Whidbey's repl tag extensions."
  {(class (byte-array 0)) (puget/tagged-handler 'whidbey/bin bin-str)
   java.net.URI           (puget/tagged-handler 'whidbey/uri str)})


(defn wrap-handlers
  "Wraps custom handlers around an existing dispatch."
  [dispatch]
  (dispatch/chained-lookup
    (when-let [escaped (seq (:escape-types options))]
      (dispatch/symbolic-lookup
        (fn lookup [t]
          (when (some #{t} escaped)
            puget/pr-handler))))
    dispatch
    tag-handlers
    puget/common-handlers))



;; ## Value Rendering

(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  [value]
  (puget/pprint-str value options))
