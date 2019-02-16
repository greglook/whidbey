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
  [project]
  (let [version (find-plugin-version project 'mvxcvi/whidbey)
        options (:whidbey project)
        {:keys [init custom-init]} (:repl-options project)]
    (-> `{:dependencies [[mvxcvi/whidbey ~(or version "RELEASE")]]
          ;; :init is run once when the server starts
          ;; :custom-init is run on session creation
          ;; ^:replace to workaround https://github.com/technomancy/leiningen/issues/878
          :repl-options {:init ^:replace (do ~init
                                             (require 'whidbey.repl)
                                             (whidbey.repl/init! ~options))
                         :custom-init ^:replace (do ~custom-init
                                                    (whidbey.repl/update-print-fn!))
                         ;; :printer is nrepl 0.5.x only
                         :nrepl-context {:interactive-eval {:printer whidbey.repl/render-str}}}}
        (vary-meta assoc :repl true))))


(defn repl-pprint
  "Adds a `whidbey/repl` profile to the project containing Whidbey's repl
  customizations. The profile is tagged with metadata which will cause it to be
  merged for the repl task."
  [project]
  (if (:whidbey/repl (:profiles project))
    project
    (let [profile (whidbey-profile project)]
      (project/add-profiles project {:whidbey/repl profile}))))
