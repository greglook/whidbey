(ns whidbey.repl
  (:require
    [clojure.data.codec.base64 :as b64]
    [clojure.tools.nrepl.middleware.pr-values :refer [pr-values]]
    [puget.data :as data]
    [whidbey.render :as render])
  (:import
    java.net.URI))


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


(defmacro extend-notation!
  "Implements the `ExtendedNotation` protocol from Puget for the given type,
  setting the rendering function and updating the reader in `*data-readers*`."
  [tag t renderer reader]
  `(when-not (extends? data/ExtendedNotation ~t)
     (data/extend-tagged-value ~t '~tag ~renderer)
     (alter-var-root #'default-data-readers assoc '~tag ~reader)))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (render/update-options! options)
  (alter-var-root #'pr-values (constantly identity))
  (let [extend-option (:extend-notation options true)
        should-extend? (case extend-option
                         true  (constantly true)
                         false (constantly false)
                         #(some (partial = %) extend-option))]
    (when (should-extend? :bin)
      (extend-notation! whidbey/bin
                        (class (byte-array 0))
                        #(apply str (map char (b64/encode %)))
                        read-bin))
    (when (should-extend? :uri)
      (extend-notation! whidbey/uri URI str read-uri))))
