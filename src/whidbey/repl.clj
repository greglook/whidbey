(ns whidbey.repl
  (:require
    [clojure.tools.nrepl.middleware.pr-values :refer [pr-values]]
    [whidbey.render :as render]
    [whidbey.types :as types]))


(defn init!
  "Initializes the repl to use Whidbey's customizations."
  [options]
  (render/update-options! options)
  (alter-var-root #'pr-values (constantly identity))
  (if (:extend-notation render/printer)
    (doseq [[tag reader] types/tag-readers]
      (alter-var-root #'default-data-readers assoc tag reader))))
