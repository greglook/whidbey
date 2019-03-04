(defproject example "0.1.0-SNAPSHOT"
  :description "example using whidbey with [nrepl 0.6.0] and :custom-init"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [nrepl "0.6.0"]]
  :plugins [[mvxcvi/whidbey "2.1.0"]]
  :middleware [whidbey.plugin/repl-pprint]
  :repl-options {:init-ns example.core
                 :init (example.core/init)
                 :custom-init (example.core/custom-init)})

