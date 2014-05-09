whidbey
=======

This project hacks into nREPL to replace the default `pr-values` middleware
with `render-value`. This watches nREPL messages for the `:renderer` key, and
uses it to produce the returned string value.

TL;DR: pretty-print REPL values by default!

## Usage

To use this middleware, add it as `:nrepl-middleware` to a Leiningen profile.
For example, to pretty-print all values with
[Puget](https://github.greglook/puget) (the main motivation of this middleware),
you can use the following:

```clojure
 :dependencies
 [[mvxcvi/puget "0.4.0"]
  [mvxcvi/whidbey "0.1.0-SNAPSHOT"]]

 :repl-options
 {:nrepl-renderer puget.printer/cprint-str
  :init
  (require 'clojure.tools.nrepl.middleware.render-values
           'puget.printer)
  :nrepl-middleware
  [clojure.tools.nrepl.middleware.render-values/render-values]}}
```

Unfortunately, this _also_ currently requires a custom version of REPLy. See
below for a more detailed explanation of why. To use this, you'll need to use a
local checkout of Leiningen with an updated version, since it's currenty not
possible to set the REPLy version with profiles or project files.

To summarize:
 1. Clone my fork of [REPLy](https://github.com/greglook/reply/tree/nrepl-renderer) with the patch and switch to the `nrepl-renderer` branch.
 2. Ensure REPLy has a `SNAPSHOT` version and `lein install` it locally.
 3. Clone Leiningen and update the `project.xml` dependency on `reply` to the custom version.
 4. Clone whidbey and `lein install` it locally.
 5. Add the configuration above to your `user` or `system` profile.
 6. Run `lein repl` and enjoy the colored goodness!

## Motivation

As I develop Clojure and interact with the REPL, I frequently used
`clojure.pprint` to get a better display of the results of my commands. Later, I
wrote [Puget](https://github.greglook/puget) to pretty print in a canonical
fashion with ANSI coloring. Soon I found myself running this after almost every
command:

```clojure
(puget.printer/cprint *1)
```

I decided that it would be really nice if the REPL just pretty-printed colored
values _for_ me, so I dove into
[Leiningen](https://github.com/technomancy/leiningen),
[REPLy](https://github.com/trptcolin/reply),
[nREPL](https://github.com/clojure/tools.nrepl).

## Learning to REPL

When you start a REPL, the basic sequence of events looks like this:

 1. Leiningen parses the `:repl-options` in your project map.
 2. Leiningen starts an nREPL server with any specified custom handler or
    middlewares. (more on this later)
 3. Leiningen starts a REPLy client with the given options and connects it to
    nREPL.
 4. The read-eval-print-loop starts.

However, you're actually interacting with a client/server model, so the text you
type into the REPL isn't directly interpreted. Instead, in must be sent to the
server and the result communicated back to the client. nREPL accomplishes this
using messages with an `:op` key. On the server side, a _handler_ and a stack of
_middleware_ functions (very much like Ring) processes the messages and send
back result messages.

For example, when you type a form like `(+ 1 2 3)`, REPLy sends the server a
message like:

```clojure
{:op "eval"
 :code "(+ 1 2 3)"
 :ns "user"}
```

nREPL's `interruptible-eval` middleware catches `eval` operations and runs a
sub-REPL to read and evaluate the input. The resulting value is built into
another message and sent back to the client's `Transport`:

```clojure
{:ns "user"
 :value 6}
```

At a higher level in the middleware stack, nREPL's `pr-eval` wraps the
`Transport` passed to later handlers. When messages are sent, the `:value` is
transformed into a string using `print-method` or `pr-dup`. This is needed
because the result has to be serialized back over the wire to the client, and
arbitrary Clojure values are not supported.

## Fixing the Problem

To add enough functionality to support colored pretty-printing, it turns out to
be necessary to modify REPLy, but fortunately not nREPL or Leiningen (code,
anyway). The following patch to REPLy's nREPL integration does the trick:

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

This change looks for an `:nrepl-renderer` key in the options passed to the
client. The value of this key should be a namespaced symbol which will resolve
to a _rendering function_ on the server. Rendering functions accept one argument
(the value to render) and return a string representation.

It is necessary to patch REPLy because the client sends `eval` ops to the server
in situations other than user input. For example, when you tab-complete a name
while typing an expression, REPLy is actually evaluating a completion function
on the server that searches the current namespace for matching symbols. The
client then `read-string`s the response to get the list of matches.

If we just pretty-printing _all_ `eval` requests, the results to requests like
these would contain extra whitespace and ASCII color codes, which break the
Clojure reader. We need to be able to select when to use a custom renderer and
when plain strings are desirable, and the REPL client is the only place we can
do that.

Ideally this patch will get merged into REPLy and nREPL will come to support the
`render-values` middleware natively, at which point this project will no longer
be necessary.

## Value Rendering

Now we're finally in a position to pretty print our REPL values! This library
provides a `render-values` middleware which replaces the built-in `pr-values`.
This watches messages for the `:renderer` key, and uses it to produce the
returned string value. By default, this falls back to `print-method` or
`print-dup`, so if you don't specify any custom function in your project's
`:repl-options` map, it will behave exactly as before.

Now, `eval` messages are passed down the stack, handled by `interruptible-eval`,
and the result sent to the `Transport` to send back to the client. The
`render-values` middleware's inserted transport processes the response message
by using the desired function to render the message value. In Puget's case, this
means returning a string with embedded ANSI color codes. When REPLy receives
this message, all it has to do is faithfully reprint the string and the user
sees nicely colored and pretty-printed text.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
