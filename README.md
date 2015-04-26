whidbey
=======

[![Dependency Status](https://www.versioneye.com/user/projects/543d75fe64e43a7498000213/badge.svg?style=flat)](https://www.versioneye.com/clojure/mvxcvi:whidbey)

This project reaches into [nREPL](https://github.com/clojure/tools.nrepl)'s
guts to replace the default `pr-values` middleware with the more general
`render-values`. This watches nREPL messages for the `:renderer` key, and uses
it to produce the returned string value.

See the [history](HISTORY.md) for more on the motivations and implementation
details behind this project.

TL;DR: pretty-print colored REPL values by default!

## Usage

The easiest way to use Whidbey is as a Leiningen plugin. Note that this requires
Leiningen version 2.5.1 or higher for profile and plugin functionality.

To pretty-print all values with [Puget](https://github.com/greglook/puget), add
the following to your `user` or `system` profile:

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

See the Puget
[`*options*`](https://github.com/greglook/puget/blob/master/src/puget/printer.clj)
var for the available configuration.

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

This is controlled by the `:extend-notation` option, which defaults to `true`.
You can disable the extensions by setting it to `false`, or for more selective
control you can specify a collection of keywords to enable. The keys match the
tag names, so currently `:bin` and `:uri` are valid.

### Troubleshooting

Sometimes, there are types which Puget has trouble rendering. These can be
excluded from pretty-printing by adding their symbol to the `:exclude-types` set
in the options. These types will be printed with Puget's 'unknown type'
rendering. If you want to use these types' `print-method` instead, set the
`:print-fallback` option to `:print`:

```clojure
:whidbey {:print-fallback :print
          :escape-types #{'datomic.db.Db 'datomic.btset.BTSet ...}
          ...}
```

Whidbey may also conflict with existing REPL customizations. If you experience
errors, you can check how the profiles are being merged using the lein-pprint or
[lein-cprint](https://github.com/greglook/lein-cprint) plugins:

```bash
$ lein with-profile +whidbey/repl cprint :repl-options
```

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
