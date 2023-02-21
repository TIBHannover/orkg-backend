# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- All entities now report the correct creation timestamps. (Closes: [#438](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/438))
- Class ID generation works correctly now. (See: [!433](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/433))

## [0.25.0] - 2023-01-24
### Fixed
- Searching for a venue during paper creation works correctly now. (See: [!380](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/380))
### Changed
- Error responses are unified to make them better consumable for clients. (Closes: [#78](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/78), [#298](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/298), [#332](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/332), [#434](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/434))
- Logos were removed from the response when updating organizations. (See: [!358](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/358))
- The `Auditable` entity / node label was removed. It was an internal implementation detail, and no know clients depend on it. (See: [!352](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/352))
### Added
- It is now possible to generate dummy data for missing entries in the Postgres database during development by executing the `populatePostgresDatabase` Gradle task. (Closes: [#237](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/237))
- Added an image repository that stores images in the database. (See: [!357](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/357))

## [0.24.0] - 2023-01-09
### Added
- Support for conference series. (Closes: [#401](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/401))

## [0.23.2] - 2022-12-21
### Changed
- Caches can now be configured individually (via [Coffee-Boots](https://github.com/stepio/coffee-boots)). (See: [!363](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/363))

## [0.23.1] - 2022-12-20
### Security
- Fixed an issue that allowed users to register and modify organizations and observatories without authorization. (Closes: [#441](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/441))

## [0.23.0] - 2022-12-16
### Security
- Fixed an issue that allowed users to register multiple times with the same email address in different casing. (See [!354](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/354))
### Added
- Caching support for `Thing`s. (See [!255](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/355))
- Added endpoint to lookup multiple classes by id. (See: [!321](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/321))

## [0.22.0] - 2022-12-07
### Added
- Caching support is now available when fetching classes, predicates, or literals from their repository, as well as on existence checks.
  [Caffeine](https://github.com/ben-manes/caffeine) is the default caching provider.
  (See [!338](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/338), [!340](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/340), [!342](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/342))
- Metrics collected via [Micrometer](https://micrometer.io/) are exposed via JMX for monitoring.
### Changed
- Paged endpoints were limited to 2500 elements per requested page.
  This limit can be changed by setting a different value, e.g. via the environment variable `SPRING_DATA_WEB_PAGEABLE_MAX_PAGE_SIZE`.

## [0.21.0] - 2022-12-02
### Added
- Deleting predicates is supported. (Closes: [#346](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/346))
### Removed
- Hawtio was removed as a dependency. It was not really used except in development, and we mostly use `jconsole` anyway.

## [0.20.0] - 2022-11-30
### Fixed
- Checking for the existence of several classes at once gives the correct result. (Closes: [#433](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/433)) 
### Added
- Obtaining predicates is cached now. (See: [!322](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/322))

## [0.19.0] - 2022-11-18
### Fixed
- When the resource's classes are updated, all classes need to exist. (Closes: [#216](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/216))
### Added
- The feature flag `orkg.features.use-neo4j-version-3` was introduced to skip initialization steps that are not compatible with Neo4j 4.x.

## [0.18.0] - 2022-11-16
### Fixed
- The pagination metadata for endpoints filtering by research fields is now correct. (Closes: [#413](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/413))
### Changed
- Searching for a label with exact matching is now case-insensitive. (Closes: [#362](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/362))
### Added
- The number of orphaned nodes in the database is now tracked via the statistics endpoint. (Closes: [#420](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/420))

## [0.17.2] - 2022-11-08
### Fixed
- Fixed a performance regression when converting resources and statements to their JSON representations.
  The error lead to the lookup table for the resource usage being calculated multiple times instead of once, issuing the same request to the database each time.

## [0.17.1] - 2022-11-08
### Changed
- The version of the PostgreSQL driver was updated to fix some connection issues that are known for the version used until now.
### Fixed
- The formatted labels feature was not fully disabled in 0.16.2. The remaining places were updated to fully disable it.

## [0.17.0] - 2022-11-08
### Added
- Added monitoring and management capabilities via Spring Boot Actuator and JMX.

## [0.16.2] - 2022-10-27
### Changed
- Disable formatted labels by default as it is the main reason for degraded performance. (Closes: [#417](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/417))

## [0.16.1] - 2022-09-14
### Fixed
- Aggregate benchmarks per research field. (Closes: [#411](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/411))
- Searching resources by class and observatory returns the correct results. (Closes: [#410](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/410))

## [0.16.0] - 2022-08-31
### Fixed
- Bulk editing statements works again. (Closes: [#308](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/308))
- Email addresses are now case-insensitive when logging in. (Closes: [#397](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/397))
- Attribution information is correct now. (Closes: [#345](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/345))
### Added
- Support formatted labels for all resources. (Closes: [#166](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/166))
- Debugging database issues in tests is now described in the developer documentation.

## [0.15.0] - 2022-08-26
### Fixed
- Use correct casing when returning entities. (Closes: [#398](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/398))
- Return correct totals when fetching content types for a research problem. (Closes: [#399](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/399))
### Added
- Add existence checks for all entities. (See [!286](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/286).)
- The old (legacy) model for "Papers with Code" (PwC) data was re-introduced behind a feature flag.
  To use it, set `orkg.features.pwc-legacy-model=true` in the configuration or via the respective environment variables.
### Deprecated
- Although the old legacy model is re-introduced, it is deprecated and will be removed in a future release.
  Please update all client code.

## [0.14.0] - 2022-08-16
### Security
- Validation of IDs is not properly enforced.
  The only valid characters are digits, numbers, colons, underscores and dashes.
### Added
- Fetch benchmarks of all research fields for PwC use-case. (See [!240](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/240).)
### Changed
- Change the PwC model for benchmark queries and extract ID constants. (Closes: [#352](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/352))
### Fixed
- Fixed issue with on fresh start script of importing research fields with correct typing. (Closes: [#383](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/383))
- Research problems are now properly loaded, even if their `featured` and `unlisted` flags are not set.

## [0.13.3] - 2022-08-05
### Changed
- Changed error handling in DOI service for better debugging.

## [0.13.2] - 2022-08-04
### Fixed
- When using Docker Compose, the `api` container will now wait for all databases to be properly started and accepting connections.
  A recent version of Docker Compose is required to be installed. (Closes: [#375](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/375))
- Creating papers with the same DOI works again, and authors are properly referenced. (Closes: [#292](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/292))
### Changed
- The RDF export will not include statements with a `<null>` resource identifier. Although this is valid, it breaks downstream clients. (Closes: [#394](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/394))
### Removed
- The `pre-commit` configuration was removed as it was not used anymore and broke a long time ago without anyone noticing.
  If you still use it, run `pre-commit uninstall` to disable the hooks, as they will not be updated anymore.

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

[unreleased]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/compare/0.25.0...master
[0.25.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.25.0
[0.24.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.24.0
[0.23.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.23.2
[0.23.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.23.1
[0.23.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.23.0
[0.22.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.22.0
[0.21.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.21.0
[0.20.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.20.0
[0.19.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.19.0
[0.18.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.18.0
[0.17.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.17.2
[0.17.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.17.1
[0.17.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.17.0
[0.16.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.16.2
[0.16.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.16.1
[0.16.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.16.0
[0.15.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.15.0
[0.14.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.14.0
[0.13.3]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.13.3
[0.13.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.13.2
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
