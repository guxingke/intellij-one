<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-one Changelog

## [Unreleased]

## [0.0.7] - 2022-10-18

### Added

- struct mapper type converter.
- external struct mapper rule config.
- debug mode for debugging config, be reload user config at most 60 seconds.

## [0.0.6] - 2022-10-15

### Added
 
- mapper user cfg beta.

### Fixed

- user config dir not exists.

## [0.0.5] - 2022-10-09

### Added

- postfix template variable order.
- postfix template expression variable.

### Fixed

- builtin templates typo
- external templates load failed

## [0.0.4] - 2022-10-07

### Changed

- user define config dir changed, from `.config/the-one-toolbox/*.yml` to `.config/the-one-toolbox/postfix/*.yml`.

### Added

- the one toolbox config file, named `.config/the-one-toolbox/config.yml`.

## [0.0.3] - 2022-10-01

### Added

- new postfix template `map`
- user define postfix template

## [0.0.2] - 2022-09-28

### Added

- new postfix template `toList`
- new postfix template `toSet`
- new postfix template `toMap`
- new postfix template `groupingBy`
- new postfix template `partitioningBy`
- new postfix template `joining`
- new postfix template `toArray`

## [0.0.1] - 2022-09-27

### Added

- new postfix template `toIdMap`
- Initial scaffold created
  from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
