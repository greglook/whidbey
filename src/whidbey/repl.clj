(ns whidbey.repl
  (:require
    [clojure.tools.nrepl.middleware.pr-values :refer [pr-values]]
    [whidbey.render :as render]))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (render/update-options! options)
  (alter-var-root #'pr-values (constantly identity)))
