# Stemmer Trie

This repository contains the code of `org.egothor.stemmer` slightly modified and renamed `io.bdrc.lucene.stemmer`. 

The main goal of this repository is
- easy Maven distribution (distribution on Sonatype ossrh)
- small jar containing only these files, not the whole egothor 2 project
- simple git access
- bug tracker

We keep track of the changes we make to the Egothor code in the [Change Log](CHANGELOG.md).

### Installation

Through maven:

```xml
    <dependency>
      <groupId>io.bdrc.lucene</groupId>
      <artifactId>stemmer</artifactId>
      <version>1.1.0</version>
    </dependency>
```

### License

The code is under the [Egothor License](LICENSE).
