(ns whidbey.plugin
  (:require
    [leiningen.core.project :as project]))


(defn whidbey-profile
  [options]
  `{:dependencies [[mvxcvi/puget "0.6.4"]
                   [mvxcvi/whidbey "0.4.0-SNAPSHOT"]]

    :injections [(do (require 'whidbey.render)
                     (alter-var-root
                       #'whidbey.render/puget-options
                       merge
                       ~options))]

    :repl-options {:nrepl-middleware [clojure.tools.nrepl.middleware.render-values/render-values]
                   :nrepl-context {:interactive-eval {:renderer whidbey.render/render-str}}}})


(defn middleware
  [project]
  (let [profile (whidbey-profile (:puget-options project))
        included (:included-profiles (meta project))]
    (prn included)
    (if (some #{::profile} included)
      project
      (-> project
          (project/add-profiles {::profile profile})
          (project/merge-profiles [::profile])))))
