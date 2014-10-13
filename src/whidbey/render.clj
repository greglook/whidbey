(ns whidbey.render
  (:require
    [puget.printer :as puget]))


(def puget-options
  "Currently configured Puget options."
  {:print-color true})


(defn render-str
  "Renders the given value to a display string by pretty-printing it using Puget
  and the configured options."
  [value]
  (puget/pprint-str value puget-options))
