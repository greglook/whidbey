(defproject mvxcvi/whidbey "0.6.0-SNAPSHOT"
  :description "nREPL middleware to allow arbitrary value rendering."
  :url "https://github.com/greglook/whidbey"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies [[mvxcvi/puget "0.7.1"]
                 [org.clojure/tools.nrepl "0.2.7"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}})
