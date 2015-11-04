(ns whidbey.types
  "Rendering extensions for various custom types such as byte arrays and URI
  strings."
  (:require
    [clojure.data.codec.base64 :as b64])
  (:import
    java.net.URI))


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


(def tag-types
  "Extra print-handlers for Whidbey's repl tag extensions."
  {(symbol "[B") {'whidbey/bin bin-str}
   'java.net.URI {'whidbey/uri str}})


(def tag-readers
  "Inverse mapping of tag symbols to reader functions."
  {'whidbey/bin read-bin
   'whidbey/uri read-uri})
