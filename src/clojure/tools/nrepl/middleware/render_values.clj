(ns clojure.tools.nrepl.middleware.render-values
  (:require
    [clojure.string :as str]
    (clojure.tools.nrepl
      middleware
      transport)
    (clojure.tools.nrepl.middleware
      interruptible-eval
      pr-values))
  (:import
    clojure.tools.nrepl.transport.Transport))


(alter-meta!
  #'clojure.tools.nrepl.middleware.interruptible-eval/interruptible-eval
  update-in [:clojure.tools.nrepl.middleware/descriptor :requires]
  disj #'clojure.tools.nrepl.middleware.pr-values/pr-values)


(defn render-values
  [handler]
  ;handler #_
  (fn [{:keys [^clojure.tools.nrepl.transport.Transport transport renderer] :as msg}]
    (let [render-value (if renderer
                         (find-var (symbol renderer))
                         (if *print-dup* print-dup print-method))

          render-transport
          (reify clojure.tools.nrepl.transport.Transport
            (recv [this] (.recv transport))
            (recv [this timeout] (.recv transport timeout))
            (send [this resp]
              (.send transport
                (if-let [[_ v] (find resp :value)]
                  (assoc resp :value
                         (-> (render-value v)
                             with-out-str
                             clojure.string/trim))))
              this))]
      (println "#'render-value =>" render-value)
      (handler (assoc msg :transport render-transport)))))


(clojure.tools.nrepl.middleware/set-descriptor!
  #'render-values
  {:requires #{}
   :expects #{"eval"}
   :handles {}})
