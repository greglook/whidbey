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


(defn- plugin-dependency
  "Looks up the version for the given plugin, or sets it to `\"RELEASE\"`.
  Returns a dependency vector entry."
  [project plugin]
  (vector
   plugin
   (or (find-plugin-version project plugin)
       "RELEASE")))


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
  "Rewrites the project to include Whidbey's customizations when the project
  includes one of the target profiles."
  [project]
  ; TODO: stop accepting :puget-options eventually.
  (let [options (:whidbey project (:puget-options project))
        active-profiles (:included-profiles (meta project))
        target-profiles (:target-profiles options #{:repl})]
    (cond
      ; FIXME: Don't activate on standalone-repls because Puget doesn't load.
      (nil? (:name project))
        project

      ; Idempotent if the project has already been updated.
      (::included (meta project))
        project

      ; Only modify the project if desired profiles are present.
      (some (set target-profiles) active-profiles)
        (-> project
            (add-dependencies
              (plugin-dependency project 'mvxcvi/whidbey))
            (add-repl-init
              `(require 'whidbey.repl)
              `(whidbey.repl/init! ~options))
            (add-nrepl-middleware
              'clojure.tools.nrepl.middleware.render-values/render-values)
            (set-interactive-eval-renderer
              'whidbey.render/render-str)
            (vary-meta assoc ::included true))

      ; Otherwise, return project unchanged.
      :else project)))
