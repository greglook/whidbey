(defproject mvxcvi/whidbey "0.4.2"
  :description "nREPL middleware to allow arbitrary value rendering."
  :url "https://github.com/greglook/whidbey"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :dependencies [[mvxcvi/puget "0.6.6"]
                 [org.clojure/tools.nrepl "0.2.6"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}})
