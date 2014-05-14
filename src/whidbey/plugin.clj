(ns whidbey.plugin
  (:require
    [leiningen.core.project :as project]))


(defn whidbey-profile
  [renderer]
  (when renderer
    `{:dependencies
      [[mvxcvi/puget "0.5.1"]
       [mvxcvi/whidbey "RELEASE"]]

      :repl-options
      {:init [(require 'clojure.tools.nrepl.middleware.render-values '~(symbol (namespace renderer)))]
       :nrepl-middleware [clojure.tools.nrepl.middleware.render-values/render-values]
       :nrepl-context {:interactive-eval {:renderer ~renderer}}}}))


(defn middleware
  [project]
  (let [renderer (or (:whidbey-renderer project)
                     'puget.printer/cprint-str)
        profile (whidbey-profile renderer)]
    (-> project
        (project/add-profiles {:whidbey profile})
        (update-in [:profiles :repl]
                   #(if % [:whidbey %] [:whidbey])))))
