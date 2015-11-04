Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Changed
- Change from `:print-handlers` to `:tag-types` using nested maps. This makes
  Leiningen profile config more composable since maps are recursively merged.

## [1.2.0] - 2015-10-29

### Changed
- Upgrade Puget to 1.0.0.

## [1.1.1] - 2015-10-20

### Changed
- Upgrade Puget to 0.9.2.

### Fixed
- Use `puget/unknown-handler` for escaped types instead of `pr-handler`.

## [1.1.0] - 2015-10-18

### Changed
- Upgrade Puget to 0.9.1.
- Keep a `PrettyPrinter` record instead of a raw options map.
- Update to use new Puget print-handler logic.

## [1.0.0] - 2015-04-26

First "stable" release!

### Changed
- Upgrade Puget to 0.8.1.
- Use new Leiningen `:repl` metadata logic to load plugin profile.

## [0.6.0] - 2015-03-11

### Added
- Add `whidbey.repl` namespace to handle initialization.
- Dependency versions are now matched to the plugins in the project map.

### Changed
- Use `:whidbey` for project map key instead of `:puget-options`.
- Upgrade Puget to 0.8.0.

### Fixed
- Remove side-effects from loading `render-values` namespace.

## [0.5.1] - 2015-02-28

### Changed
- Upgrade Puget to 0.7.1.

## [0.5.0] - 2015-02-11

### Changed
- Upgrade Puget to 0.7.0.
- Upgrade tools.nrepl to 0.2.7.

## [0.4.2] - 2014-12-28

### Changed
- Upgrade Puget to 0.6.6.

## [0.4.1] - 2014-10-14

### Changed
- Upgrade tools.nrepl to 0.2.6.

## [0.4.0] - 2014-10-13

### Changed
- Leiningen plugin profile is now namespaced.
  [#12](//github.com/greglook/whidbey/issues/12)
- Upgrade Puget to 0.6.4.
- Use separate options from Puget to prevent interference.
- Prevent nREPL's built-in `pr-values` middleware from causing problems by
  replacing it with `identity`.

## [0.3.3] - 2014-09-23

### Added
- Allow coloring to be disabled via `:puget-options`.
  [#9](//github.com/greglook/whidbey/issues/9)

## [0.3.2] - 2014-06-26

### Added
- Pass `:puget-options` in project map to renderer.

### Changed
- Assume `puget.printer/cprint-str` as the rendering function.

## [0.2.2] - 2014-06-19

### Changed
- Upgrade Puget to 0.5.2

### Fixed
- Fix issue when user had existing `:repl` profile customizations.

## [0.2.0] - 2014-05-14

### Added
- Add `whidbey.plugin` namespace to automatically enable rendering in Leiningen
  `repl` tasks.

[Unreleased]: https://github.com/greglook/whidbey/compare/1.0.0...HEAD
[1.2.0]: https://github.com/greglook/whidbey/compare/1.1.1...1.2.0
[1.1.1]: https://github.com/greglook/whidbey/compare/1.1.0...1.1.1
[1.1.0]: https://github.com/greglook/whidbey/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/greglook/whidbey/compare/0.6.0...1.0.0
[0.6.0]: https://github.com/greglook/whidbey/compare/0.5.1...0.6.0
[0.5.1]: https://github.com/greglook/whidbey/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/greglook/whidbey/compare/0.4.2...0.5.0
[0.4.2]: https://github.com/greglook/whidbey/compare/0.4.1...0.4.2
[0.4.1]: https://github.com/greglook/whidbey/compare/0.4.0...0.4.1
[0.4.0]: https://github.com/greglook/whidbey/compare/0.3.3...0.4.0
[0.3.3]: https://github.com/greglook/whidbey/compare/0.3.2...0.3.3
[0.3.2]: https://github.com/greglook/whidbey/compare/0.2.2...0.3.2
[0.2.2]: https://github.com/greglook/whidbey/compare/0.2.0...0.2.2
[0.2.0]: https://github.com/greglook/whidbey/compare/0.1.0...0.2.0
