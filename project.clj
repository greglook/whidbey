(defproject mvxcvi/whidbey "2.0.0"
  :description "nREPL middleware to allow arbitrary value rendering."
  :url "https://github.com/greglook/whidbey"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]
  :eval-in-leiningen true
  :min-lein-version "2.8.2"

  :dependencies
  [[mvxcvi/puget "1.1.0"]
   [org.clojure/data.codec "0.1.1"]])
