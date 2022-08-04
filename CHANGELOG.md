# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- When using Docker Compose, the `api` container will now wait for all databases to be properly started and accepting connections.
  A recent version of Docker Compose is required to be installed. (Closes: [#375](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/375))
### Changed
- The RDF export will not include statements with a `<null>` resource identifier. Although this is valid, it breaks downstream clients. (Closes: [#394](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/394))

## [0.13.1] - 2022-08-02
### Fixed
- Bundles do not contain duplicate statements anymore. (Closes: [#393](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/393))

## [0.13.0] - 2022-08-01
### Changed
- The RDF dumps are no longer streamed due to technical issues.

## [0.12.0] - 2022-07-19
### Fixed
- Fixed issue with unexpected behaviour of add paper with merge flag. (Closes: [#379](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/379))
- Deleted papers do not appear in the list of papers anymore. (Closes: [#382](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/382))
### Added
- Support all entity types in object endpoint request. (Closes: [#387](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/387))
- Create additional indexes in Neo4j for heavily used nodes, improving query performance.

## [0.11.0] - 2022-05-19
### Fixed
- Fixed issue with the statistics page for counting the number of research fields. (Closes: [#337](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/337))
- Fixed memory issue when exporting all data to RDF by streaming the data instead of loading everything into memory first.
### Added
- Support for adding users and organizations in observatories. (Closes: [#294](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/294))
### Changed
- The "development" profile is no longer the default profile when running the application. However, the `bootRun` target now loads the "development" profile to be compatible with the old behavior.

## [0.10.1] - 2022-04-14
### Fixed
- Fixed issue of updating conference metadata. (Closes: [#374](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/374))

## [0.10.0] - 2022-04-13
### Fixed
- Fixed issue that lead to data corruption when saving statements.
- Labels are allowed to be empty again, as they were before. (Closes: [#372](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/372))
### Added
- Handling resource types dynamically while publishing them with DOI. (Closes: [#322](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/322))

## [0.9.1] - 2022-03-18
### Fixed
- The SPI package was missing in the list of packages for entity scanning, leading to crashes when trying to find contributors.

## [0.9.0] - 2022-03-18
### Fixed
- Prevent use of newlines in labels. (Closes: [#347](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/347))
- Removed caching of classes. (Closes: [#368](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/368))
### Added
- Support updating selected properties of a class via PATCH. (Closes: [#368](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/369))
- Support of different types of organizations. (Closes. [#357](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/357))

## [0.8.0] - 2022-02-03
### Added
- A combined list containing all content types is now provided. (Closes: [#315](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/315))

## [0.7.0] - 2021-12-13
### Security
- Upgrade Log4j to version 2.15.0 because of a [security vulnerability](https://logging.apache.org/log4j/2.x/security.html). The codebase should not be affected, because it is [not used by default in Spring](https://spring.io/blog/2021/12/10/log4j2-vulnerability-and-spring-boot).

## [0.6.2] - 2021-11-02
### Fixed
- White/Black listing of classes in Bundle fetch. (Closes: [#318](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/318))
### Added
- Support setting classes for contributions in the paper endpoint. (Closes: [#339](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/339))

## [0.6.1] - 2021-07-21
### Fixed
- Put `@Transient` annotation on field to prevent warnings when querying PwC datasets.

## [0.6.0] - 2021-07-21
### Fixed
- Stats endpoint include the missing classes and enable dynamic classes on-the-fly. (Closes: [#317](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/317))
- Summaries of PWC datasets are now filtered by problem ID. (Closes: [#316](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/316))
- The endpoint for querying the research problems of an observatory now returns all research problems assigned to the observatory, as well as all research problems of all papers maintained by the observatory. (Closes: [#307](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/307))
- URIs can now be updated again. (Closes: [#297](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/297))
- DOIs can now be created even if the author does not have a ORCID ID. (Closes: [#321](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/321))
- Upgrade TestContainers library to solve build issues on Windows. (Closes: [#324](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/324))
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

[unreleased]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/compare/0.13.1...master
[0.13.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.13.1
[0.13.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.13.0
[0.12.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.12.0
[0.11.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.11.0
[0.10.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.10.1
[0.10.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.10.0
[0.9.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.9.1
[0.9.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.9.0
[0.8.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.8.0
[0.7.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.7.0
[0.6.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.6.2
[0.6.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.6.1
[0.6.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.6.0
[0.5.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.5.0
[0.4.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.4.0
[0.3.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.3
[0.2.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.2
[0.1.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.1
