# Change log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/). It follows [some conventions](http://keepachangelog.com/).

## [1.1.0] - 2018-05-04

### Added
- the Reduce class now optimizes a Trie in a non-destructive way. It may be quite costly.

### Removed
- most of the optimization classes were removed because they were changing the result of invalid lookups (looking for word not in the original Trie would give results)

## [1.0.0] - 2017-04-20

Changes from Egothor2 v4.0.4:

### Added
- `Cell.getCommand()`
- `Trie.getRoot()`
- `Trie.getCommandVal()`
- `Row.toString()`, `Trie.toString()`

### Changes
- integration of code sent by the Egothor author in a private correspondance
- use Files API ([commit](https://github.com/BuddhistDigitalResourceCenter/stemmer/commit/a08f960b98cbecabfd9f8020dcc2cfd5fe077ad4))
- replace StringBuffer by StringBuilder ([commit](https://github.com/BuddhistDigitalResourceCenter/stemmer/commit/9a48758d9273af05b7e52962543fcee26d5ba0a9))
- `Trie.getRow()` is now public

### Fixed
- fix `ArrayIndexOutOfBounds` Exception in reduce ([commit](https://github.com/BuddhistDigitalResourceCenter/stemmer/commit/16c253ed7a31fd3652b1a6b1abfb4f12c32acce9))
- fix Java warnings and unclosed stream ([commit](https://github.com/BuddhistDigitalResourceCenter/stemmer/commit/70632e55a9361cadd80c6e1e3edac7aa95909f2e))
- fix invalid JavaDoc ([commit](https://github.com/BuddhistDigitalResourceCenter/stemmer/commit/c3dcbb8d7f7046392ff320bbc85c82de7955e582))
- update license in files to version 2.0