Motivation and History
======================

As I develop Clojure and interact with the REPL, I frequently used
`clojure.pprint` to get a better display of the results of my commands. Later, I
wrote [Puget](https://github.com/greglook/puget) to pretty print in a canonical
fashion with ANSI coloring. Soon I found myself running this after almost every
command:

```clojure
(cprint *1)
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

To add enough functionality to support colored pretty-printing, it turned out to
be necessary to modify REPLy, but fortunately not nREPL or Leiningen. The change
looks for an `:nrepl-context` map in the options passed to the client. The
values specified under `:interactive-eval` are merged with the nREPL message for
interactive evaluations.

It was necessary to patch REPLy because the client sends `eval` ops to the
server in situations other than user input. For example, when you tab-complete a
name while typing an expression, REPLy is actually evaluating a completion
function on the server that searches the current namespace for matching symbols.
The client then `read-string`s the response to get the list of matches.

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
