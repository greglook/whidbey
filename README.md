whidbey
=======

[![Dependency Status](https://www.versioneye.com/clojure/mvxcvi:whidbey/badge.svg)](https://www.versioneye.com/clojure/mvxcvi:whidbey)
[![Join the chat at https://gitter.im/greglook/whidbey](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/greglook/whidbey)

This is a plugin for [Leiningen](http://leiningen.org/) which changes the REPL
to pretty-print results with [Puget](https://github.com/greglook/puget).

Internally, Whidbey reaches into [nREPL](https://github.com/clojure/tools.nrepl)
to replace the default `pr-values` rendering middleware.  See the
[history](HISTORY.md) for more on the motivations and implementation details
behind this project.

## Usage

To use Whidbey, add it to the `:plugins` vector in your `user` or `system`
profile. Note that this requires Leiningen version 2.5.1 or higher for profile
and plugin functionality.

[![Clojars Project](http://clojars.org/mvxcvi/whidbey/latest-version.svg)](http://clojars.org/mvxcvi/whidbey)

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

Additionally, Whidbey adds some convenience tagged-literal extensions for binary
data and URIs. The extensions update the `default-data-readers` var to support
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
be added by providing a `:print-handlers` dispatch function.

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
