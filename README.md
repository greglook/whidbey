whidbey
=======

This project hacks into [nREPL](https://github.com/clojure/tools.nrepl) to
replace the default `pr-values` middleware with the more general
`render-values`. This watches nREPL messages for the `:renderer` key, and uses
it to produce the returned string value.

TL;DR: pretty-print REPL values by default!

## Usage

To use this middleware, add it as `:nrepl-middleware` to a Leiningen profile.
For example, to pretty-print all values with
[Puget](https://github.com/greglook/puget) (the main motivation of this project),
you can use the following:

```clojure
:dependencies
[[mvxcvi/puget "0.5.1"]
 [mvxcvi/whidbey "0.1.0-SNAPSHOT"]]

:repl-options
{:init (require 'clojure.tools.nrepl.middleware.render-values 'puget.printer)
 :nrepl-middleware [clojure.tools.nrepl.middleware.render-values/render-values]
 :nrepl-context {:interactive-eval {:renderer puget.printer/cprint-str}}}
```

This _also_ currently requires a snapshot version of REPLy. See below for a more
detailed explanation of why. You'll need to use a local checkout of Leiningen
with an updated version, since it's currenty not possible to set the REPLy
version with profiles or project files.

To summarize:
 1. Clone this repo and `lein install` it locally.
 2. Clone [REPLy](https://github.com/trptcolin/reply), ensure it has a `0.3.1-SNAPSHOT` version or higher, and `lein install` it locally.
 3. Clone [Leiningen](https://github.com/technomancy/leiningen) and update the `project.clj` dependency on `reply` to the version above.
 4. Switch your local `lein` command to a symlink to the `bin/lein` script in the Leiningen repo. You'll need to bootstrap on the first run.
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
values _for_ me, so I dove into the Leiningen/REPLy/nREPL stack.

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
_middleware_ functions (very much like
[Ring](https://github.com/ring-clojure/ring)) process the messages and send
result messages back to the client.

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

At a higher level in the middleware stack, nREPL's `pr-values` wraps the
`Transport` passed to later handlers. When messages are sent, the `:value` is
transformed into a string using `print-method` or `print-dup`. This is needed
because the result has to be serialized back over the wire to the client, and
arbitrary Clojure values are not supported.

## Towards a Solution

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
+        message-to-send (merge (get-in options [:nrepl-context :interactive-eval])
+                               {:op "eval" :code form :id command-id})
         read-input-line-fn (:read-input-line-fn options)]
     (session-sender message-to-send)
     (reset! current-command-id command-id)
```

This change looks for an `:nrepl-context` map in the options passed to the
client. The values specified under `:interactive-eval` are merged with the
nREPL message for interactive evaluations.

It is necessary to patch REPLy because the client sends `eval` ops to the server
in situations other than user input. For example, when you tab-complete a name
while typing an expression, REPLy is actually evaluating a completion function
on the server that searches the current namespace for matching symbols. The
client then `read-string`s the response to get the list of matches.

If we just pretty-printed _all_ `eval` requests, the results to requests like
these would contain extra whitespace and ANSI color codes, which break the
Clojure reader. We need to be able to select when to use a custom renderer and
when plain strings are desirable, and the REPL client is the only place we can
do that.

## Value Rendering

Now we're finally in a position to pretty print our REPL values! This library
provides a `render-values` middleware which replaces the built-in `pr-values`.
This watches messages for the `:renderer` key, and uses it to produce the
returned string value.

The value of `:renderer` should be a symbol which resolves to a _rendering
function_ on the server. Rendering functions accept one argument (the value to
render) and return a string representation. If not provided, `render-values`
falls back to `print-method` or `print-dup`, so if you don't specify anything
the REPL will behave exactly as before.

Now, `eval` messages are passed down the stack, handled by `interruptible-eval`,
and the result sent to the `Transport` to send back to the client. The
`render-values` middleware's inserted transport processes the response message
by using the desired function to render the message value. In Puget's case, this
means returning a string with embedded ANSI color codes. When REPLy receives
this message, all it has to do is faithfully reprint the string and the user
sees nicely colored and pretty-printed text.

## Project Status

Currently, this requires quite a bit of setup. The following changes will make
things a lot nicer:
- [X] [REPLy #138](https://github.com/trptcolin/reply/pull/138) to support
  message context on interactive evals.
- [ ] [REPLy release 0.3.1](https://github.com/trptcolin/reply) so that it
  doesn't need to be installed locally.
- [ ] [Leiningen](https://github.com/technomancy/leiningen) upgrade to REPLy
  version 0.3.1 or higher, so that it doesn't need to be cloned locally.
- [ ] [NREPL-55](http://dev.clojure.org/jira/browse/NREPL-55) for a better way
  to control rendering middleware in the REPL.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
