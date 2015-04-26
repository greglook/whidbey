(defproject mvxcvi/whidbey "1.0.0"
  :description "nREPL middleware to allow arbitrary value rendering."
  :url "https://github.com/greglook/whidbey"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]
  :eval-in-leiningen true

  :dependencies [[mvxcvi/puget "0.8.1"]
                 [org.clojure/data.codec "0.1.0"]])
