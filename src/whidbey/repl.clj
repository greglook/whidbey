(ns whidbey.repl
  (:require
    (clojure.tools.nrepl.middleware
      [pr-values :refer [pr-values]]
      [render-values :refer [render-values]])
    [clojure.tools.nrepl.server :refer [default-handler]]
    [whidbey.render :as render]))


(defn wrap-nrepl-handler!
  "Alters the nrepl server handler by wrapping it with Whidbey's `render-values`
  middleware and deactivating the default `pr-value`."
  []
  ;(alter-var-root #'default-handler #'render-values)
  (alter-var-root #'pr-values (constantly identity)))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (render/update-options! options)
  (wrap-nrepl-handler!))
