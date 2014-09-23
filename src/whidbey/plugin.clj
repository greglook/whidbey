(ns whidbey.plugin
  (:require
    [leiningen.core.project :as project]))


(defn whidbey-profile
  [options]
  `{:dependencies
    [[mvxcvi/puget "RELEASE"]
     [mvxcvi/whidbey "RELEASE"]]

    :injections
    [(do (require 'puget.printer)
         (alter-var-root
           #'puget.printer/*options*
           puget.printer/merge-options
           ~options))]

    :repl-options
    {:nrepl-middleware
     [clojure.tools.nrepl.middleware.render-values/render-values]
     :nrepl-context
     {:interactive-eval {:renderer puget.printer/pprint-str}}}})

(def default-puget-options
  {:print-color true})

(defn- inject-whidbey
  "Adds :whidbey as a merged default to the given profile. Returns an updated
  profile value."
  [profile]
  (if (vector? profile)
    (if (some #{:whidbey} profile)
      profile
      (vec (cons :whidbey profile)))
    (if profile
      [:whidbey profile]
      [:whidbey])))


(defn middleware
  [project]
  (let [profile (whidbey-profile (merge default-puget-options
                                        (:puget-options project)))]
    (-> project
        (project/add-profiles {:whidbey profile})
        (update-in [:profiles :repl] inject-whidbey))))
