whidbey
=======

This is a plugin for [Leiningen](http://leiningen.org/) which changes the REPL
to pretty-print results with [Puget](https://github.com/greglook/puget).

![repl demo](demo.gif)

Internally, Whidbey integrates with the [nREPL](https://github.com/nrepl/nrepl)
`pr-values` middleware to provide a custom pretty-printer for the results of
evaluated forms in the REPL. See the [history doc](HISTORY.md) for more on the
motivations and implementation details behind this project.


## Usage

To use Whidbey, add it to the `:plugins` vector in your `user` or `system`
profile. Note that this requires Leiningen version 2.8.2 or higher for the
necessary nREPL and plugin functionality.

[![Clojars Project](http://clojars.org/mvxcvi/whidbey/latest-version.svg)](http://clojars.org/mvxcvi/whidbey)

Since Leiningen has deprecated implicit plugin middleware, you'll need to
activate it by ading the following to your profile as well:

```clojure
:middleware [whidbey.plugin/repl-pprint]
```

### Configuration

Whidbey passes rendering options into Puget from the `:whidbey` key in the
profile map:

```clojure
:whidbey {:width 180
          :map-delimiter ""
          :extend-notation true
          :print-meta true
          :color-scheme {:delimiter [:blue]
                         :tag [:bold :red]
                         ...}
          ...}
```

See the [`puget.printer`](https://greglook.github.io/puget/api/puget.printer.html)
namespace for the available configuration.

If you feel like adjusting Whidbey's configuration at runtime, you can use the
`whidbey.repl/update-options!` function. This will affect all subsequent
messages rendered.

If you need to further customize the responses from the REPL, Whidbey respects
any `:print-options` set on the `:op :eval` message. These will be merged into
the normal rendering configuration, but will not affect subsequent messages.

### Tag Extensions

Whidbey adds some convenience tagged-literal extensions for binary data and
URIs. The extensions update the `default-data-readers` var to support
round-tripping the tagged representations:

```clojure
=> (java.net.URI. "http://github.com/greglook")
#whidbey/uri "http://github.com/greglook"

=> (.getBytes "foo bar baz")
#whidbey/bin "Zm9vIGJhciBiYXo="

=> #whidbey/bin "b25lIG1vcmUgdGltZSwgbXVzaWNzIGdvdCBtZSBmZWVsaW5nIHNvIGZyZWU="
#whidbey/bin "b25lIG1vcmUgdGltZSwgbXVzaWNzIGdvdCBtZSBmZWVsaW5nIHNvIGZyZWU="
```

This is controlled by the `:extend-notation` option. Other type extensions can
be added by providing a `:tag-types` map. This should map type symbols to a map
with a tag symbol key pointing to a formatting function. When the type is
encountered, it will be rendered as a tagged literal with a form from calling
the formatter on the value.

For example, to render class values as tagged types, you can add this to your
`:whidbey` config:

```clojure
:tag-types
{java.lang.Class {'java/class #(symbol (.getName %))}}}
```

If the type name or the formatter function are not available at load time, you
can quote them to suppress evaluation until those types are printed.

### Troubleshooting

Sometimes, there are types which Puget has trouble rendering. These can be
excluded from pretty-printing by adding their symbol to the `:escape-types` set
in the options. These types will be rendered with the normal Clojure printer.
If you want to use these types' `print-method` instead, set the
`:print-fallback` option to `:print`:

```clojure
:whidbey {:print-fallback :print
          :escape-types #{'datomic.db.Db 'datomic.btset.BTSet ...}
          ...}
```

Whidbey may also conflict with other REPL customizations. If you experience
errors, you can check how the profiles are being merged using the lein-pprint or
[lein-cprint](https://github.com/greglook/lein-cprint) plugins:

```bash
$ lein with-profile +whidbey/repl cprint :repl-options
```


## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
