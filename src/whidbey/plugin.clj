(ns whidbey.plugin
  (:require
    [leiningen.core.project :as project]))


(defn whidbey-profile
  [options]
  `{:dependencies [[mvxcvi/puget "0.7.0"]
                   [mvxcvi/whidbey "0.5.0"]]

    :injections [(do (require 'whidbey.render)
                     (alter-var-root
                       #'whidbey.render/puget-options
                       merge
                       ~options))]

    :repl-options {:nrepl-middleware [clojure.tools.nrepl.middleware.render-values/render-values]
                   :nrepl-context {:interactive-eval {:renderer whidbey.render/render-str}}}})


(defn middleware
  [project]
  (let [options (:puget-options project)
        included (:included-profiles (meta project))]
    (if (some #{::profile} included)
      project
      (-> project
          (project/add-profiles {::profile (whidbey-profile options)})
          (project/merge-profiles [::profile])))))
