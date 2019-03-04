(ns example.core)

(defn init [& args]
  (println "Hello from :init\n"))

(defn custom-init [& args]
  (println "Hello from :custom-init\n")
  (println "You may want check: nrepl.version/version")
  (println "nrepl 0.6.0 is now supported by whidbey :)\n"))
