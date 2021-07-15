# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- Stats endpoint include the missing classes and enable dynamic classes on-the-fly. (Closes: [#317](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/317))
- Summaries of PWC datasets are now filtered by problem ID. (Closes: [#316](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/316))
- The endpoint for querying the research problems of an observatory now returns all research problems assigned to the observatory, as well as all research problems of all papers maintained by the observatory. (Closes: [#307](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/307))
### Breaking  
- Making organizations and observatories object properties snake-case. (Closes: [!196](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/196))

## [0.5.0] - 2021-07-12
### Changed
- Bundle fetch of statements is now sorted by default on the creation date of the statement in a descending manner.
- Revert caching of predicates because of unintended side effects (when doing things we really shouldn't do). (Closes: [#319](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/319))

## [0.4.0] - 2021-06-14
### Changed
- Return scores in PwC data as their original string value. (Closes: [#310](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/310))

## [0.3.0] - 2021-05-12
### Added
- New points to support the Papers With Code (PWC) use case. Results from AI publications can now be queried for display in leaderboards. (Closes: [#263](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/263))
### Changed
- Dropped `rdf4k` dependency by replacing it with RDF4J. This should fix the build failures on GitLab as a side effect, caused by the shutdown of Bintray/JCenter. The Kotlin-specific extensions were not used in the codebase, so the library effectively only pulled in RDF4J.

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
- Fixed broken/wrong output in API documentation. The examples should now reflect the real responses everywhere. (Closes: [#274](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/274))

## [0.1.0] - 2021-03-30
### Added
- This CHANGELOG file. Finally!

[unreleased]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/compare/0.5.0...master
[0.5.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.5.0
[0.4.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.4.0
[0.3.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.3
[0.2.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.2
[0.1.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.1
