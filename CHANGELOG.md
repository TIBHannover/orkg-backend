# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2021-05-04
### Added
- Caching of predicates to improve performance.
- A script to load an existing Neo4j database dump into a Docker container (development).
  It should also run properly on Windows. (Fingers crossed…)
- Annotation processing during development is properly supported now. It may require you to run the "clean" task once before the stubs are generated and picked up by your IDE.
- Shared run configurations for IntelliJ IDEA to support the development workflow.
### Changed
- Default roles will be created on start-up if they do not exist already.
### Fixed
- Top contributions are now sorted correctly. (Closes: [#265](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/265))
- The ID generators will now skip over IDs already used by entities due to manual creation, instead of generating an error. (Closes: [#94](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/94))

## [0.1.0] - 2021-03-30
### Added
- This CHANGELOG file. Finally!

[unreleased]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/compare/0.2...master
[0.2.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.2
[0.1.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.1
