whidbey
=======

[![Dependency Status](https://www.versioneye.com/user/projects/543d75fe64e43a7498000213/badge.svg?style=flat)](https://www.versioneye.com/user/projects/543d75fe64e43a7498000213)

This project reaches into [nREPL](https://github.com/clojure/tools.nrepl)'s
guts to replace the default `pr-values` middleware with the more general
`render-values`. This watches nREPL messages for the `:renderer` key, and uses
it to produce the returned string value.

See the [history](HISTORY.md) for more on the motivations and implementation
details behind this project.

TL;DR: pretty-print colored REPL values by default!

## Usage

The easiest way to use Whidbey is as a Leiningen plugin. Note that this requires
Leiningen version 2.4.2 or higher for functionality in REPLy 0.3.1.

[![Clojars Project](http://clojars.org/mvxcvi/whidbey/latest-version.svg)](http://clojars.org/mvxcvi/whidbey)

To pretty-print all values with [Puget](https://github.com/greglook/puget) (the
main motivation of this project), you can add the following in your `user`,
`system`, or `repl` profile:

```clojure
:plugins [[mvxcvi/whidbey "0.4.1"]]

; customize printing options:
:puget-options {:width 180
                :map-delimiter ""
                :print-meta true
                :color-scheme {:delimiter [:blue]
                               :tag [:bold :red]
                               ...}}
```

See the Puget
[`*options*`](https://github.com/greglook/puget/blob/master/src/puget/printer.clj)
var for more possibilities.

### Troubleshooting

This may conflict with existing REPL customizations, so if necessary you can add
the [profile configuration](src/whidbey/plugin.clj) yourself.

If you experience errors, you can check how the profiles are being merged using
the lein-pprint or [lein-cprint](https://github.com/greglook/lein-cprint)
plugins:

```bash
$ lein with-profile +repl cprint :injections :repl-options
```

## Project Status

Whidbey used to require quite a bit of setup. Fortunately, the following changes
have made things a lot nicer:
- [X] [REPLy #138](https://github.com/trptcolin/reply/pull/138) to support
  message context on interactive evals.
- [X] [REPLy release 0.3.1](https://github.com/trptcolin/reply) so that it
  doesn't need to be installed locally.
- [X] [Leiningen](https://github.com/technomancy/leiningen) upgrade to REPLy
  version 0.3.1 or higher, so that it doesn't need to be cloned locally. (Done
  as of 2.4.2)
- [ ] [NREPL-55](http://dev.clojure.org/jira/browse/NREPL-55) for a better way
  to control rendering middleware in the REPL.

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
