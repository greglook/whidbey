whidbey
=======

This project hacks into nREPL to replace the default `pr-values` middleware
with `render-value`. This watches messages for the `:renderer` key, and uses it
to produce the returned string value. By default, this falls back to
`print-method` or `print-dup`, giving backwards compatibility with `pr-values`.

## Usage

To use this middleware, add it as `:nrepl-middleware` to a Leiningen profile.
For example, to pretty-print all values with
[Puget](https://github.com/greglook/puget) (the main motivation of this
middleware), you can use the following:

```clojure
 :dependencies
 [[mvxcvi/puget "0.4.0-SNAPSHOT"]
  [mvxcvi/whidbey "0.1.0-SNAPSHOT"]]

 :repl-options
 {:nrepl-renderer puget.printer/cprint-str
  :init
  (require 'clojure.tools.nrepl.middleware.render-values
           'puget.printer)
  :nrepl-middleware
  [clojure.tools.nrepl.middleware.render-values/render-values]}}
```

Unfortunately, this _also_ currently requires a custom version of
[REPLy](https://github.com/trptcolin/reply) with the following tweak:

```diff
--- a/src/clj/reply/eval_modes/nrepl.clj
+++ b/src/clj/reply/eval_modes/nrepl.clj
@@ -74,7 +74,10 @@
   (let [command-id (nrepl.misc/uuid)
         session (or (:session options) @current-session)
         session-sender (nrepl/client-session client :session session)
-        message-to-send {:op "eval" :code form :id command-id}
+        message-to-send (let [msg {:op "eval" :code form :id command-id}]
+                          (if-let [renderer (:nrepl-renderer options)]
+                            (assoc msg :renderer renderer)
+                            msg))
         read-input-line-fn (:read-input-line-fn options)]
     (session-sender message-to-send)
     (reset! current-command-id command-id)
```

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
