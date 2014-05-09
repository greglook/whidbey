(ns clojure.tools.nrepl.middleware.render-values
  (:require
    [clojure.string :as str]
    (clojure.tools.nrepl
      [middleware :as middleware]
      transport)
    (clojure.tools.nrepl.middleware
      interruptible-eval
      pr-values))
  (:import
    clojure.tools.nrepl.transport.Transport))


(defn- print-renderer
  "Uses print-dup or print-method to render a value to a string."
  [v]
  (let [printer (if *print-dup* print-dup print-method)
        writer (java.io.StringWriter.)]
    (printer v writer)
    (str writer)))


(defn- wrap-renderer
  "Wraps a Transport with code which renders the value of messages sent to
  it using the provided function."
  [^Transport transport render-value]
  (reify Transport
    (recv [this]
      (.recv transport))
    (recv [this timeout]
      (.recv transport timeout))
    (send [this resp]
      (.send transport
        (if-let [[_ v] (find resp :value)]
          (let [r (str/trim-newline (render-value v))]
            (assoc resp :value r))
          resp))
      this)))


(defn render-values
  "Middleware wrapper which wraps the handler transport in a rendering layer."
  [handler]
  (fn [{:keys [transport renderer] :as msg}]
    (->>
      (if renderer
        (find-var (symbol renderer))
        print-renderer)
      (wrap-renderer transport)
      (assoc msg :transport)
      handler)))


(middleware/set-descriptor!
  #'render-values
  {:requires #{}
   :expects #{"eval"}
   :handles {}})


; Here's where things get ugly. We need to prevent the native `pr-values`
; middleware from loading, but the `interruptible-eval` middleware explicitly
; requires it.
(alter-meta!
  #'clojure.tools.nrepl.middleware.interruptible-eval/interruptible-eval
  update-in [:clojure.tools.nrepl.middleware/descriptor :requires]
  disj #'clojure.tools.nrepl.middleware.pr-values/pr-values)
