(ns whidbey.plugin
  "This namespace runs inside of Leiningen and rewrites the project map to
  include the customizations provided by Whidbey."
  (:require
    [leiningen.core.project :as project]))


;; These versions are spliced into the project dependencies.
(def puget-version "0.8.0-SNAPSHOT")
(def whidbey-version "0.6.0-SNAPSHOT")


(defn- add-dependencies
  "Adds some dependencies to the end of the current vector."
  [project & deps]
  (update-in project [:dependencies] concat deps))


(defn- add-repl-init
  "Adds the given repl initialization forms to the `:init` key in
  `:repl-options`. Any existing initialization will happen before the
  form is executed."
  [project & forms]
  (update-in project
             [:repl-options :init]
             (fn [current]
               (if current
                 `(do ~current ~@forms)
                 `(do ~@forms)))))


(defn- add-nrepl-middleware
  "Adds the middleware identified by the given symbol to the *front* of the
  current nrepl-middleware list in the project map."
  [project sym]
  (update-in project
             [:repl-options :nrepl-middleware]
             #(into [sym] %)))


(defn- set-interactive-eval-renderer
  "Sets the nrepl renderer for the interactive-eval context to the function
  named by the given symbol."
  [project sym]
  (assoc-in project
            [:repl-options :nrepl-context :interactive-eval :renderer]
            sym))


(defn middleware
  "Rewrites the project to include Whidbey's customizations when the `:repl`
  profile is active."
  [project]
  (cond
    ; Idempotent if the project has already been updated.
    (::included (meta project))
      project

    ; Only modify the project for repl invocations.
    (some #{:repl} (:included-profiles (meta project)))
      (let [options (:whidbey project (:puget-options project))]
        (-> project
            (add-dependencies
              `[mvxcvi/puget ~puget-version]
              `[mvxcvi/whidbey ~whidbey-version])
            (add-repl-init
              `(require 'whidbey.repl)
              `(whidbey.repl/init! ~options))
            (add-nrepl-middleware
              'clojure.tools.nrepl.middleware.render-values/render-values)
            (set-interactive-eval-renderer
              'whidbey.render/render-str)
            (vary-meta assoc ::included true)))

    ; Otherwise, return project unchanged.
    :else project))
