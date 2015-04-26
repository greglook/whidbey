(ns whidbey.plugin
  "This namespace runs inside of Leiningen and rewrites the project map to
  include the customizations provided by Whidbey."
  (:require
    [leiningen.core.project :as project]))


(defn- find-plugin-version
  "Looks up the plugins in the project map and tries to find the version
  specified for the given symbol. Returns nil if none matches."
  [project plugin]
  (try
    (some (fn [[p v]] (when (= p plugin) v))
          (:plugins project))
    (catch Exception e
      nil)))


(defn- whidbey-profile
  "Constructs a profile map for enabling the repl hooks."
  [version options]
  (->
    `{:dependencies [[mvxcvi/whidbey ~(or version "RELEASE")]]
      :repl-options {:nrepl-middleware [clojure.tools.nrepl.middleware.render-values/render-values]
                     :nrepl-context {:interactive-eval {:renderer whidbey.render/render-str}}
                     :init (do (require 'whidbey.repl)
                               (whidbey.repl/init! ~options))}}
    (vary-meta assoc :repl true)))


(defn middleware
  "Rewrites the project to include Whidbey's customizations when the project
  includes one of the target profiles."
  [project]
  (cond
    ; FIXME: Don't activate on standalone-repls because Puget doesn't load.
    (nil? (:name project))
      project

    ; Idempotent if the profile already exists.
    (:whidbey/repl (:profiles project))
      project

    :else
      (let [options (:whidbey project)
            version (find-plugin-version project 'mvxcvi/whidbey)
            profile (whidbey-profile version options)]
        (project/add-profiles project {:whidbey/repl profile}))))
