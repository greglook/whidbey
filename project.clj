(defproject mvxcvi/whidbey "0.4.0-SNAPSHOT"
  :description "nREPL middleware to allow arbitrary value rendering."
  :url "https://github.com/greglook/whidbey"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies
  [[org.clojure/tools.nrepl "0.2.3"]]

  :profiles
  {:dev
   {:dependencies
    [[org.clojure/clojure "1.6.0"]]}})
