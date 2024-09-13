# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.67.0] - 2024-09-13
### Added
- Target classes of Rosetta Stone Statement Templates are now being assigned the same description as the Rosetta Stone Statement Template.
  (See: [!1064](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1064))

### Changed
- All 'not modifiable' error responses now return status code 403.
  (See: [!1056](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1056))
- Class URIs are now required to be absolute.
  (Closes: [#220](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/220))
- Trailing slashes are now optional for all endpoints.
  (See: [!1058](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1058))
- It is now possible to assign a conference series as the organization of a comparison.
  (See: [!1063](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1063))
- The endpoint for publishing comparisons has been reworked.
  (See: [!1034](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1034))

## [0.66.0] - 2024-09-04
### Added
- Added content-type endpoint for publishing literature lists.
  (See: [!1035](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1035))
- Added content-type endpoint for publishing smart reviews.
  (See: [!1038](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1038))
- Added content-type endpoint for deleting comparison related resources.
  (See: [!1052](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1052))
- Added content-type endpoint for deleting comparison related figures.
  (See: [!1053](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1053))

### Changed
- Literal values for more XSD data types are now being validated.
  (See: [!1041](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1041),
        [!1042](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1042))
- The order of references of a comparison is now retained when updating a comparison.
  (See: [!1046](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1046))

### Fixed
- Fixed validation of xsd:duration literal values.
  (See: [!1043](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1043))
- Fixed a possible status 500 when fetching timeline of certain resources.
  (See: [!1044](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1044))
- Fixed exception message when trying to create a rosetta stone statement with missing input positions.
- Fixed newly created comparison related resources not being linked to comparison.
  (See: [!1050](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1050))
- Fixed optional fields for comparison related resource create requests being mandatory.
- Fixed optional fields for comparison related resource update requests being mandatory.
- Fixed newly created comparison related figures not being linked to comparison.
  (See: [!1051](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1051))
- Fixed optional fields for comparison related figure create requests being mandatory.

### Removed
- Removed `statement_id` field from statement update requests because it had no effect.
  (See: [!1054](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1054))

## [0.65.0] - 2024-08-26
### Added
- Added support for extraction method field on templates.
  (See: [!1026](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1026))
- Added support for ISBN identifiers on papers.
  (See: [!1032](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1032))
- Added support for ISSN identifiers on papers.
  (See: [!1033](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1033))

### Changed
- Adjusted paper publishing endpoint to reflect front-end behavior.
  (See: [!1030](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1030))
- Integer literals (xsd:integer) can now exceed 32 bit range.

### Fixed
- The widget endpoint once again returns results when looking up entities by DOI.
  (See: [!1037](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1037))
- The rest-api server hostname is now the same across all documentation snippets.

## [0.64.1] - 2024-08-09
### Fixed
- Fixed escaping of `\` symbols for N-Triple literal value serialization.
  (See: [!1027](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1027))

## [0.64.0] - 2024-08-08
### Added
- Added filtering parameter `doi_prefix` to paper content-type endpoint.
  (Closes: [#577](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/577))
- The endpoint `/api/classes` now supports filtering by `created_by`, `created_at_start`, `created_at_end`.
  (See: [!1018](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1018))
- The endpoint `/api/predicates` now supports filtering by `created_by`, `created_at_start`, `created_at_end`.
  (See: [!1019](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1019))
- The endpoint `/api/literals` now supports filtering by `created_by`, `created_at_start`, `created_at_end`.
  (See: [!1020](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1020))
- Added content-type endpoint for updating rosetta stone templates.
  (See: [!1016](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1016))

### Changed
- Updated template model diagrams.
  (See: [!1014](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1014))
- The rest api documentation now uses an ORKG theme.
  (See: [!1004](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1004))
- The endpoints `/api/classes`, `/api/predicates` and `/api/literals` no longer require a trailing `/`.
  (See: [!1018](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1018),
        [!1019](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1019),
        [!1020](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1020))
- The widget endpoint now always returns the latest resource related to a DOI and supports returning PaperVersion instances.
  (See: [!1023](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1023))
- Literal data types now must be an absolute IRI.
  (See: [!1022](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1022))

### Fixed
- Literal values created during content-type creation are now also validated for known XSD data types.
  (See: [!1013](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1013))
- Papers containing no contributions but rosetta stone statements are no longer being unlisted automatically.
  (See: [!1015](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1015))
- URI validation now matches RFC 3987 as described in the [W3C XSD specification](https://www.w3.org/TR/2012/REC-xmlschema11-2-20120405/datatypes.html#anyURI).
  (See: [!1021](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1021))
- Literal boolean values "0" and "1" are now considered valid.
- Literals are now escaped according to the [W3C RDF N-Triple specification](https://www.w3.org/TR/rdf12-n-triples/#canonical-ntriples).
  (See: [!1025](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1025))

## [0.63.0] - 2024-07-31
### Added
- Added support for mentionings on paper content-types.
  (See: [!1007](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1007),
        [!1008](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1008))
- Added content-type endpoint for deleting unused rosetta stone templates.
  (See: [!1009](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1009))

### Changed
- Literal values are now validated for known XSD data types.
  (Closes: [#578](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/578))
- Reorganized API documentation.
  (See: [!1003](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1003))
- Simplified error messages for rosetta stone statement creation.
  (Closes: [#575](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/575))

### Fixed
- Fixed URI parsing for publication info url and author website sometimes causing status 500.
  (Closes: [#578](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/578))

## [0.62.0] - 2024-07-22
### Added
- Added content-type endpoint for creating smart reviews.
  (See: [!995](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/995))
- Added content-type endpoint for creating smart review sections.
  (See: [!996](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/996))
- Added content-type endpoint for updating smart reviews.
  (See: [!999](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/999))
- Added content-type endpoint for updating smart review sections.
  (See: [!998](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/998))
- Added content-type endpoint for deleting smart review sections.
  (See: [!997](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/997))
- Added acknowledgements to smart review responses.
  (See: [!1000](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1000))

### Changed
- The fields `authors`, `sdgs`, `organizations`, `observatories` and `sections` are now optional when creating a literature list.
  (See: [!1001](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1001))

### Fixed
- Fixed retrieval of research fields for smart reviews.
  (See: [!995](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/995))
- Fixed deletion process of literature list sections.
  (See: [!1001](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1001))

## [0.61.2] - 2024-07-04
### Fixed
- The target class of a template is now validated correctly.

## [0.61.1] - 2024-07-01
### Removed
- The following statement related endpoints have beeen removed from the api
  (Closes [#549](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/549)):
  - `/api/statements/subject/{subjectId}`
  - `/api/statements/subject/{subjectId}/predicate/{predicateId}`
  - `/api/statements/predicate/{predicateId}`
  - `/api/statements/predicate/{predicateId}/literal/{literal}`
  - `/api/statements/predicate/{predicateId}/literals`
  - `/api/statements/object/{objectId}`
  - `/api/statements/object/{objectId}/predicate/{predicateId}`
- The following endpoint has been removed from the api
  (See: [!967](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/967)):
  - `/api/user/role`

## [0.61.0] - 2024-06-28
### Added
- Added endpoint for creating literature list sections at a specific index.
  (See: [!974](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/974))
- Added endpoint for deleting individual literature list sections.
  (See: [!975](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/975))
- Download documentation and source artifacts in all Kotlin subprojects.
  (See: [!991](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/991))
- Added import endpoints for resources, classes and predicates from external ontologies.
  (See: [!970](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/970))
- Added curation endpoint for fetching predicates without descriptions.
  (See: [!993](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/993))
- Added curation endpoint for fetching classes without descriptions.
  (See: [!994](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/994))
- Added endpoint for soft-deleting rosetta stone statements.
  (See: [!982](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/982))
- Added endpoint for deleting rosetta stone statements. Requires curator status.
  (See: [!983](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/983))

### Fixed
- Fixed predicate descriptions missing from all requests except when being fetched by id.
  (See: [!984](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/984))
- Fixed class descriptions missing from all requests except when being fetched by id.
  (See: [!985](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/985))
- It is no longer possible to modify published literature lists when using literature list content-type endpoints.
  (See: [!976](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/976))
- It is no longer possible to create or update a rosetta stone statement with a nested rosetta stone statement as an input.
  (See: [!988](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/988))

## [0.60.0] - 2024-06-21
### Added
- Added validation support for xsd:double, xsd:duration, xsd:dateTime and xsd:time primitive data types.
  (See: [!981](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/981))

### Changed
- The two distinct parameter sets for the endpoint `/api/resources` have been merged and can now be used in arbitrary combinations.
  (See: [!968](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/968))

### Fixed
- Resources can no longer be deleted when still being used in a rosetta stone statement.
  (See: [!971](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/971))
- Resources can now be deleted even when they are still used as a subject in a statement.
  (See: [!971](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/971))
- Predicates can no longer be deleted when still being used in a rosetta stone statement.
  (See: [!972](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/972))
- Fixed empty rosetta stone statement objects not being correctly retrieved.
  (See: [!980](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/980))

## [0.59.0] - 2024-06-07
### Added
- Enabled formatted labels on resources via content-negotiation.
  (Closes: [#562](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/562))
- Added support for descriptions on literature list section entries.
  (See: [!960](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/960))
- Added acknowledgements to literature list responses.
  (See: [!962](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/962))
- Added an endpoint to fetch linked contents of published literature lists.
  (See: [!963](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/963))
- Formatted labels of Rosetta Stone Templates are now persisted with each Rosetta Stone Statement version.
  (See: [!961](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/961))

### Fixed
- Error responses with status code 406 and 415 no longer return an empty body.
  (See: [!957](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/957))
- Fixed content type head version parsing.
- Null values provided for identifiers no longer causes a status 500.
  (Closes: [#570](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/570))
- Fixed several validation issues for lists.
  (See: [!963](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/963))

## [0.58.0] - 2024-05-30
### Added
- Added endpoints for creating, retrieving and updating rosetta stone statements.
  (See: [!927](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/927),
        [!938](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/938))
- The target class of rosetta stone templates is now included in the response.
  (See: [!927](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/927))
- Added mandatory `example_usage` field to rosetta stone templates.
  (See: [!943](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/943))
- Added a `created_by` field to all version entries of content-type endpoints.
  (See: [!950](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/950))

### Changed
- It is now possible to create empty literature list text sections.
  (See: [!949](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/949))
- Rosetta Stone Templates no longer require an object position.
  (See: [!935](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/935))
- The first input position of a rosetta stone templates is now required to be the subject position.
  (See: [!947](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/947))
- Rejected requests are now mapped to status 400 bad request instead of status 500 internal server error.
  (See: [!934](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/934))

### Fixed
- Descriptions and formatted labels are now properly validated when creating or updating a template.
  (See: [!940](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/940))
- Fixed descriptions of content-types not accepting new lines.
  (See: [!942](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/942))

## [0.57.0] - 2024-05-08
### Added
- Added basic support for generating OpenAPI documentation.
  (See: [!736](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/736))

### Changed
- It is no longer possible to create resources with class `RosettaStoneStatement` using the resource endpoints.
  (See: [!928](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/928))
- It is no longer possible to use resources with class `RosettaStoneStatement` in the subject position of statements.
  (See: [!928](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/928))

### Fixed
- Template properties can now be created with a `max_count` of zero.
  (See: [!929](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/929))
- Fixed predicate description creation within content-type requests.
  (See: [!926](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/926))
- Updating list section now properly deletes obsolete sections.
  (Closes: [#563](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/563))
- Providing a title in literature list update requests is no longer required.
  (Closes: [#565](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/565))

## [0.56.0] - 2024-05-03
### Added
- Added `uri` field to `datatype` of literal template property representations.
  (See: [!921](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/921))

### Fixed
- Fixed `unlisted_by` property for resources always being `null` when fetched via statement endpoints.
  (See: [!916](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/916))
- Fixed possible status 500 when attempting to write statements containing unlisted resources.

## [0.55.0] - 2024-04-24
### Added
- Added content-type endpoints for creating and updating literature lists and literature list sections.
  (See: [!884](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/884),
        [!885](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/885),
        [!886](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/886),
        [!887](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/887))
- Added content-type endpoint for creating rosetta-stone templates.
  (See: [!891](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/891))

### Fixed
- Template properties created using the template content-type endpoint are now 0-indexed.
  (See: [!896](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/896))
- Updating the research field of a comparison or paper now only updates the graph if it has changed.
  (See: [!897](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/897))

## [0.54.0] - 2024-04-10
### Added
- Added generic content-type endpoint that can fetch papers, comparisons, visualizations, templates, literature lists and smart reviews at once.
  (See: [!869](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/869))
- Added content-type endpoints for updating comparisons, comparison related resources and comparison related figures.
  (See: [!863](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/863))

### Changed
- Aligned template property model with front-end implementation.
  (See: [!874](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/874))
- Users can now delete their own predicates, as long as they are not used in any statement.
  (Closes: [#559](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/559))

### Fixed
- The template update endpoint no longer returns an error when updating a template with same target class that it is already assigned to.
  (See: [!875](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/875))
- The template listing endpoint no longer throws an error when trying to parse malformed templated, instead they are filtered out.
  (See: [!877](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/877))

## [0.53.0] - 2024-04-02
### Added
- Listing templates (content-type) now supports the following additional filtering parameters: `created_at_start`, `created_at_end`, `observatory_id`, `organization_id`, `include_subfields`.
  (See: [!866](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/866))
- The following new metrics were added to the `/api/statistics` endpoint:
  (See: [!868](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/868))
  - orphan-resource-count
  - orphan-predicate-count
  - orphan-literal-count
  - orphan-class-count
  - unused-resource-count
  - unused-predicate-count
  - unused-literal-count
  - unused-class-count

### Changed
- Changed template target class representation to include `label` and `uri`.
  (See: [!867](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/867),
        [!871](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/871))

### Fixed
- It is now possible to create and update templates without a `description` or `formatted_label`.
  (See: [!870](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/870))
- It is now possible to create and update templates with a `min_count` and `max_count` of `0`.
  (See: [!870](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/870))
- Fixed request parameter documentation for listing comparisons.

### Removed
- The following endpoints have been removed from the api:
  (Closes: [#546](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/546))
  - `/api/classes/Paper/featured/resources/?featured={featured}`
  - `/api/classes/Paper/resources/?verified={verified}`
  - `/api/classes/Paper/unlisted/resources/?unlisted={unlisted}`
  - `/api/classes/{id}/resources`
  - `/api/comparisons/metadata/featured?featured={featured}`
  - `/api/comparisons/metadata/featured`
  - `/api/comparisons/metadata/unlisted?unlisted={unlisted}`
  - `/api/comparisons/metadata/unlisted`
  - `/api/contributions/metadata/featured?featured={featured}`
  - `/api/contributions/metadata/featured`
  - `/api/contributions/metadata/unlisted?unlisted={unlisted}`
  - `/api/contributions/metadata/unlisted`
  - `/api/observatories/{id}/comparisons`
  - `/api/organizations/{id}/comparisons`
  - `/api/papers/{id}/metadata/featured`
  - `/api/papers/{id}/metadata/unlisted`
  - `/api/papers/{id}/metadata/verified`
  - `/api/problems/metadata/featured?featured={featured}`
  - `/api/problems/metadata/unlisted?unlisted={unlisted}`
  - `/api/problems/{id}/metadata/featured`
  - `/api/problems/{id}/metadata/unlisted`
  - `/api/resources/metadata/featured?featured={featured}`
  - `/api/resources/metadata/featured`
  - `/api/resources/metadata/unlisted?unlisted={unlisted}`
  - `/api/resources/metadata/unlisted`

## [0.52.0] - 2024-03-25
### Added
- Added a new endpoint to fetch statement counts of papers.
  (See: [!808](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/808))

### Changed
- Improved traceability of label constraint violations, when using content type endpoints.
  (See: [!858](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/858))

### Fixed
- Fixed optional fields being ignored when updating a template or template property-
  (See: [!861](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/861))
- Fixed `is_anonymized` field not being persisted, when using the content-type endpoint for comparisons. 
  (See: [!862](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/862))
- All fields of a paper are now validated before the graph is modified, when using the content type endpoint (v2) for papers.

## [0.51.0] - 2024-03-19
### Added
- Added content-type endpoints for updating templates and template properties.
  (See: [!854](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/854))
- Added content-type endpoints for fetching rosetta-stone templates.
  (See: [!782](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/782))

### Changed
- Changed database reload script to force-use docker compose plugin.
  (Closes: [#548](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/548))

### Fixed
- Fixed error when trying to delete resources as a curator
  (Closes: [#552](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/552))

## [0.50.1] - 2024-03-14
### Fixed
- Fixed some endpoints unexpectedly returning status 500.
  (See: [!853](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/853))

## [0.50.0] - 2024-03-13
### Added
- Added content-type endpoints for updating template instances.
  (See: [!762](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/762))
- Added sustainable development goals field (`sdgs`) to comparison, literature list and smart review content-type representations.
  (See: [!847](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/847),
        [!843](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/843),
        [!845](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/845))
- Added support for filtering comparisons, literature lists and smart reviews by sustainable development goals field (`sdg`), when using content-type endpoints.
  (See: [!848](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/848),
        [!844](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/844),
        [!846](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/846))
- The development docker image now provides a way to attach a remote debugger to the rest-api.
  (See: [!836](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/836))
- Added local SimComp configuration to docker image.
  (See: [!817](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/817))

### Fixed
- Fixed research field endpoints not returning published literature lists and published smart reviews.
- Fixed publishing of smart reviews.
  (See: [!851](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/851))

## [0.49.0] - 2024-03-07
### Added
- Added content-type endpoints for fetching literature lists.
  (See: [!767](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/767))
- Added content-type endpoints for fetching smart reviews.
  (See: [!816](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/816))
- Listing comparisons now supports additional filtering parameters in arbitrary combinations: `exact`, `created_at_start`, `created_at_end`, `observatory_id`, `organization_id`.
  (See: [!819](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/819))
- It is now possible to assign papers to sustainable development goals.
  (See: [!830](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/830),
        [!831](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/831),
        [!832](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/832))
- It is now possible to filter papers by sustainable development goals.
  (See: [!829](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/829))
- Added support for template property placeholders.
  (See: [!823](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/823))
- Added support for template property descriptions.
  (See: [!824](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/824))
- It is now possible to define filter configurations for observatories.
  (See: [!616](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/616))
- Paper resources can now be fetched using a filter configuration.
  (See: [!616](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/616))
- Resources can now be fetched by label and a given base class.
  (See: [!616](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/616))
- Added content-type endpoints for fetching template instances.
  (See: [!758](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/758))

## [0.48.1] - 2024-03-01
### Fixed
- Fixed benchmark quality kind class alignment.
  (See: [!825](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/825))

## [0.48.0] - 2024-02-28
### Added
- Added `modifiable` field to papers (read-only).
  (See: [!806](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/806))
- Added new statistics endpoints.
  (See: [!803](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/803))
- Added new observatory update endpoint.
  (See: [!812](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/812))
- Added a new endpoint for fetching statements.
  (See: [!809](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/809))
- Added support for assigning sustainable development goals to observatories.
  (See: [!815](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/815))

### Changed
- It is now possible to use the field `name` instead of `observatory_name` when creating a new observatory.
  (See: [!813](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/813))
- Fields for paper update requests are now only evaluated when they are different to the existing fields of the paper.
  (See: [!818](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/818))

### Fixed
- The publication info is now properly validated when creating a paper.
  (See: [!801](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/801))
- Fixed benchmark related queries using the wrong quantity kind class.
  (Closes: [#530](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/530))
- Fixed startup crash when neo4j database was empty.
  (See: [!814](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/814))

## [0.47.0] - 2024-02-09
### Added
- Added `modifiable` field for classes, predicates, literals, statements and lists (read-only).
  (See: [!777](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/777),
        [!786](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/786),
        [!787](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/787),
        [!788](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/788),
        [!789](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/789))
- Added visibility filter parameter to `/api/classes/{id}/resources` endpoint.
  (See: [!790](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/790))

### Changed
- The venue of papers and comparison responses now contains an additional id of the resource.
  (See: [!772](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/772))
- Multiple headers are now exposed to browser scripts.
  (See: [!774](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/774))
- It is again possible to assign research field R11 (Science) to content-types.
  (See [!781](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/781))

### Fixed
- The author list of papers and comparisons is no longer being deleted when updating with `null` author list.
  (See [!773](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/773))
- Fixed missing label validation for content-type endpoints.
  (See: [!776](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/776),
        [!780](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/780),
        [!800](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/800))

## [0.46.0] - 2024-01-29
### Added
- Added graph model documentation for content-types
  (See: [!751](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/751))

### Changed
- Content-type responses now support multiple identifiers per type
  (See: [!756](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/756))

### Fixed
- Fixed metadata creation for templates
  (See: [!761](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/761))
- Reserved classes can no longer be used in paper and contribution requests
  (See: [!764](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/764))
- Fixed comparison endpoints returning status 500
  (See: [!771](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/771))
- Fixed paper and comparison endpoints not returning all available data

## [0.45.0] - 2024-01-23
### Added
- Added `modifiable` field to resources (read-only).
  (See: [!757](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/757))
- Listing papers (content-type) now supports additional filtering parameters in arbitrary combinations: `exact`, `verified`, `created_at_start`, `created_at_end`, `observatory_id`, `organization_id`.
  (See: [!732](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/732))

### Changed
- Comparison content-type endpoints now return a full list of previous versions.
  (Closes: [!537](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/537))

### Fixed
- It is no longer possible to assign research field "Science" (R11) using content-type endpoints.
  (Closes: [!538](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/538))
- It is now possible to assign multiple DOIs to a single resource.
  (Closes: [!540](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/540))
- Fixed documentation for visibility field.
  (See: [!753](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/753))

## [0.44.0] - 2023-12-20
### Fixed
- Users can again delete resources which they created.
  (Closes: [#535](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/535))
- All container tests are run again.
  (Closes: [#532](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/532))
### Added
- Documentation for all endpoints related to research fields was added.
  (Closes: [#531](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/531))
- Author information can be added when publishing a paper or comparison.
  (See: [!738](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/738))

## [0.43.0] - 2023-12-13
### Added
- Added a new endpoint for updating papers.
  (See: [!716](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/716))
- Added new endpoints for fetching templates and template properties.
  (See: [!683](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/683))
- Added new endpoints for creating templates and template properties.
  (See: [!684](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/684))
- Added a new endpoint for creating visualizations.
  (See: [!677](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/677))
- The endpoint `/api/resources/` now supports additional filtering parameters (`visibility`, `created_by`, `created_at_start`, `created_at_end`, `observatory_id`, `organization_id`).
  (Closes [#351](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/351),
  See: [!694](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/694))
- Added new metadata field for paper content-type: `unlisted_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))
- Added new metadata fields for contribution content-type: `extraction_method`, `created_at`, `created_by`, `unlisted_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))
- Added new metadata field for comparison content-type: `unlisted_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))
- Added new metadata fields for comparison related resource content-type: `created_at`, `created_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))
- Added new metadata fields for comparison related figure content-type: `created_at`, `created_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))
- Added new metadata field for visualization content-type: `unlisted_by`.
  (See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688))

### Changed
- All endpoints with write access to the graph now require authentication.
  (See: [!706](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/706),
  [!709](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/709))
- The endpoint `/api/resources/` now throws an error when queried with invalid sorting parameters.
  (See: [!694](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/694))

### Fixed
- All content-type endpoints now return status `201 CREATED` instead of `204 NO CONTENT`, as per documentation.
  (See: [!724](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/724))
- Domain constraints are now properly enforced across all endpoints.
  (Closes [#524](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/524),
  See: [!688](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/688),
  [!713](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/713),
  [!714](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/714),
  [!699](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/699))
- Fixed contributor attribution for publishing endpoints.
  (See: [!711](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/711))
- Fixed error responses being empty in some cases.
  (See: [!704](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/704))
- Fixed identifiers having no contributor id set when using content-type endpoints.
  (See: [!721](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/721))
- Fixed incorrect predicate used when referring a comparison to a research field, using content-type endpoints.
  (See: [!715](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/715))
- Fixed missing sorting parameters for `/api/papers/` (legacy).
  (See: [!703](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/703))

## [0.42.0] - 2023-11-22
### Added
- Added new endpoints for creating comparisons
  (See: [!672](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/672))
- Added new endpoints for creating comparison related resources and figures
  (See: [!673](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/673))
- Predicate ids are now exported to json, along with their label.
  (See: [!685](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/685))

## [0.41.0] - 2023-11-10
### Added
- It is now possible to update the extraction method for resources.
  (Closes: [#520](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/520))
### Fixed
- Fixed `/api/stats/top/contributors` only returning contributions.
  (Closes: [#517](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/517))
- Fixed request field name for extraction method not being snake case when updating resources.
  (Closes: [#520](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/520))
- Fixed list endpoints returning results for non-list resources
  (See: [!692](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/692))

## [0.40.0] - 2023-10-19
### Added
- It is now possible to filter papers by `visibility`, `research_field` and optionally `include_subfields`
  (See: [!668](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/668))
- It is now possible to filter comparisons by `visibility`, `research_field` and optionally `include_subfields`
  (See: [!669](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/669))
- It is now possible to filter visualizations by `visibility`, `research_field` and optionally `include_subfields`
  (See: [!670](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/670))

## [0.39.0] - 2023-10-16
### Added
- Added export of unpublished comparisons.
  (Closes: [#499](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/499))
### Fixed
- The new paper endpoint now creates pre-existing identifiers.
  (Closes: [#509](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/509))

## [0.38.0] - 2023-09-21
### Added
- Added new endpoints to fetch research field hierarchies
  (See: [!606](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/606))
- Added new endpoints for creating papers and contributions.
  (Closes: [#279](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/279),
  See: [!582](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/582))
### Changed
- Statements are paged by default now. (See: [!327](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/327))
- Fetching several comparisons no longer returns previous version comparisons when no filter is set or filtering by visibility.
  (See: [!630](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/630))
### Fixed
- Lists that still have elements can now be deleted.
  (See: [!627](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/627))
- Lists can now be updated with an empty element list.
  (See: [!627](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/627))

## [0.37.0] - 2023-09-08
### Added
- Added new endpoints for fetching visualizations.
  (See: [!624](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/624))
- Added new endpoints for fetching comparisons.
  (See: [!622](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/622))
- Added a new endpoint to fetch license information for external sites.
  (Closes: [#495](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/495))
- The widget API was extended to return the class of the resources found.
  (Closes: [#500](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/500))
- Added support for ordered lists.
  (Closes: [#175](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/175))
### Changed
- Literal datatypes need to be a valid URI or prefixed with `xsd:`.
### Fixed
- Benchmark queries can handle optional models and codes now.
  (Closes: [#492](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/492))
- Literal datatypes are now correctly exported to RDF.
  (Closes: [#503](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/503))

## [0.36.0] - 2023-08-28
### Added
- Added new endpoints to fetch papers and contributions
  (Closes: [#295](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/295),
  [#286](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/286),
  See: [!501](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/501))
### Changed
- Moved parts of the documentation to Antora
  (See: [!605](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/605))
- The endpoint `/api/statements/{thingId}/bundle` now returns status 404 when the given thing does not exist in the graph
  (Closes: [#494](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/494))
- The endpoint `/api/widgets/` now ignores casing when matching papers by title
  (Closes: [#497](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/497))
### Fixed
- Fixed entity endpoints sometimes returning status 500 when searching by label
  (See: [!602](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/602))
- Fixed missing snippets from REST-API documentation.
  (See: [!604](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/604))
- Fixed incorrect entity count for class hierarchy endpoint `/api/classes/{id}/hierarchy`
  (See: [!608](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/608))
- Fixed internal server error for endpoint `/api/classes/roots`
  (See: [!608](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/608))

## [0.35.1] - 2023-07-11
### Changed
- Linking to existing IDs in the paper and object endpoints uses `ThingId` lookup and doesn't rely on `@type` property anymore.
  (See: [!594](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/594))

## [0.35.0] - 2023-06-22
### Added
- Added an alternative endpoint to query statements with a given predicate and literal called `/api/statements/predicate/{predicate_id}/literals/?q={literal}`
  (See: [!578](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/578))
- It is now possible to search observatories by name via `/api/observatories/?q={searchString}`
    (See: [!581](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/581))
- It is now possible to fetch stats for about single observatory via `/api/stats/observatories/{id}`
  (See: [!581](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/581))
- It is now possible to fetch all research fields that belong to an observatories via `/api/observatories/research-fields`
  (See: [!581](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/581))
### Changed
- Fuzzy search by label now returns more results
  (See: [!579](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/579))

## [0.34.1] - 2023-06-06
### Fixed
- Fixed problems with hyphens when using search endpoints
  (Closes: [#487](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/487))
- Stabilized sort order for search endpoints
  (Closes: [#486](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/486))

## [0.34.0] - 2023-06-05
### Added
- Papers can now automatically be un-listed and re-listed based on a quality score.
  The quality score is determined by the following metrics:
  - At least one author has to be present
  - At least one contribution with properties has to be present
  - The title has to be not-blank
  
  (See: [!512](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/512))
- Unlisted resources now include a property called `unlisted_by`, indicating the user who unlisted the resource.
  (See: [!512](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/512))

### Changed
- The endpoint `/api/resources/{id}/timeline` no longer returns results past the creation time of the resource.
  (See: [!545](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/545))
- Templates now use the SHACL shapes vocabulary.
  (Closes: [#484](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/484))
- Changing the visibility of a resource now properly sets the `unlisted_by` property.
  (See: [!565](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/565))
- The endpoint `/api/observatories/research-field/{id}/observatories` was moved to `/api/observatories/?research_field={id}`.
  (See: [#498](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/498))
- The endpoint `/api/observatories/stats/observatories` was moved to `/api/stats/observatories` and now returns the total count of resources (`total`) for each observatory and the `resources` field has been renamed to `papers`.
  (Closes: [#406](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/185);
  See: [#498](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/498))
- The following endpoints now feature pagination:
    - `/api/observatories/`
    - `/api/observatories/{id}/papers`
    - `/api/observatories/{id}/comparisons`
    - `/api/observatories/{id}/users`
    - `/api/stats/observatories`.

  (Closes: [#185](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/185),
  [#268](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/268),
  [#403](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/403),
  [#405](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/45),
  [#450](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/450);
  See: [#498](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/498))
- The observatory representation now contains the members.
  (Closes: [#404](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/404);
  See: [#498](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/498))

## [0.33.0] - 2023-05-30
### Added
- The backend version can now be determined via `/api/version`.
  (See: [!543](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/543))
- RDF-Dumps now include the class hierarchy.
  (See: [!542](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/542))

### Changed
- Fuzzy search by label now returns more relevant results
  (See: [!541](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/541))

### Fixed
- Fixed query for finding statements by subject class, predicate id and object label
  (Closes: [#485](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/485))
- Creating labels that exceed the maximum allowed length will no longer result in "500 Internal server error".
  (See: [!558](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/558))

## [0.32.0] - 2023-05-24
### Changed
- All IDs need to be globally unique now.
  (See: [!515](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/515))

## [0.31.1] - 2023-05-17
### Fixed
- Loading of predicate and class description works again.
  (See: [!537](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/537))
- Fixed queries for finding contributions related to research problem.
  (See: [!536](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/536))

## [0.31.0] - 2023-05-16
### Fixed
- Fixed an issue with looking up venues during paper creation.
  (See: [!528](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/528))
- Does not cause internal errors anymore when invalid `Accept` header field is sent.
  (See: [!521](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/521))
- Fixed an issue when creating RDF dumps with the default configuration.
  (See: [!534](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/534))
- RDF dumps can be created on Windows again.
  (Closes: [#480](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/480))
### Added
- Support for fulltext search on labels of resources, classes, predicates and literals.
  (Closes: [#211](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/211),
           [#212](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/212);
   See: [!504](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/504),
        [!505](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/505),
        [!506](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/506),
        [!507](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/507))
- Class hierarchies are now supported.
  (Closes: [#421](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/421))

## [0.30.4] - 2023-05-15
### Fixed
- The default file permissions on the RDF dump were changed to not cause issues in deployment. (See: [!526](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/526))
- When adding a paper, publication year and month are now set correctly.
  (Closes: [#476](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/476))
- Updating a single statement works now.
  (Closes: [#477](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/477))
- All issues when updating organization are fixed now.
  (See: [!527](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/527))

## [0.30.3] - 2023-05-05
### Changed
- Reverted checks on statement deletion, because it breaks the frontend.
  (See: [!523](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/523))

## [0.30.2] - 2023-05-04
### Changed
- The "featured" and "unlisted" flags were redesigned to make the queries more consistent and less error-prone.
  This only affects the internal data structures, the behavior for clients stays the same.
  (See: [!499](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/499), [!519](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/519))
### Removed
- The endpoint for fetching literals (unpaged) was removed. (See: [!510](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/510))

## [0.30.1] - 2023-04-05
### Fixed
- "Bulk deleting" a single statement does work now. (See: [!493](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/493))

## [0.30.0] - 2023-04-04
### Security
- Some open endpoints were closed, and deleting statements requires ownership or curator status now. (See: [!492](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/492))
### Removed
- The legacy Papers with Code (Pw) model support has been removed. (See: [!488](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/488))

## [0.29.0] - 2023-03-28
### Fixed
- Deleting statements should not result in "500 Server Error" anymore. (Closes: [#467](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/467))
### Added
- It is now possible to have discussions about entities. (See: [!454](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/454))

## [0.28.1] - 2023-03-27
### Fixed
- The RDF export does not use the atomic move operation anymore. (See: [!483](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/483))
- The changelog for research fields was fixed to provide correct results. (See [!480](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/480))
### Changed
- The benchmark endpoints are now paged. (Closes: [#461](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/461))

## [0.28.0] - 2023-03-23
### Fixed
- DOIs are compared insensitive now, as mandated by the [DOI handbook](https://www.doi.org/doi_handbook/2_Numbering.html#2.4). (Closes: [#254](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/254))
- Logos can be set when creating organization. (Closes: [#462](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/462))
### Removed
- Removed deprecated logo field from organization response. (See: [!463](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/463))
- Removed unused data field from organization update request. (See: [!431](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/431))
### Changed
- The endpoint `/api/resource/{id}/contributors` was moved to `/api/resource/{id}/timeline` and changed to bin its results per minute per contributor.
  The old endpoint for contributors was replaced with an implementation that returns a distinct and paged set of contributor IDs.
  (See: [!423](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/423))

## [0.27.1] - 2023-03-10
### Fixed
- Fixed incorrect statement count and performance issue for widget endpoint (Closes: [#459](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/459))

## [0.27.0] - 2023-03-02
### Fixed
- Searching for research fields now works correctly with featured and unlisted flags. (Closes: [#392](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/392))
### Added
- It is now possible to get a list of top authors of a comparison via `/comparisons/{id}/authors`. (Closes: [#440](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/440))
- It is now possible to find all papers connected to a specific resource via `/api/papers/?linkedTo={id}`. (Closes: [#415](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/415))

## [0.26.0] - 2023-02-27
### Fixed
- All entities now report the correct creation timestamps. (Closes: [#438](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/438))
### Changed
- Upgrade Neo4j to version 4.4. (Closes: [#178](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/178), [#257](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/257))
### Removed
- Removed the endpoint to fetch user information. The contributors endpoint should be used instead. (Closes: [#184](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/184))

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

[unreleased]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/compare/0.67.0...master
[0.67.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.67.0
[0.66.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.66.0
[0.65.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.65.0
[0.64.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.64.1
[0.64.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.64.0
[0.63.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.63.0
[0.62.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.62.0
[0.61.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.61.2
[0.61.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.61.1
[0.61.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.61.0
[0.60.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.60.0
[0.59.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.59.0
[0.58.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.58.0
[0.57.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.57.0
[0.56.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.56.0
[0.55.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.55.0
[0.54.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.54.0
[0.53.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.53.0
[0.52.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.52.0
[0.51.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.51.0
[0.50.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.50.1
[0.50.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.50.0
[0.49.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.49.0
[0.48.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.48.1
[0.48.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.48.0
[0.47.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.47.0
[0.46.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.46.0
[0.45.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.45.0
[0.44.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.44.0
[0.43.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.43.0
[0.42.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.42.0
[0.41.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.41.0
[0.40.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.40.0
[0.39.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.39.0
[0.38.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.38.0
[0.37.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.37.0
[0.36.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.36.0
[0.35.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.35.1
[0.35.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.35.0
[0.34.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.34.1
[0.34.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.34.0
[0.33.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.33.0
[0.32.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.32.0
[0.31.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.31.1
[0.31.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.31.0
[0.30.4]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.30.4
[0.30.3]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.30.3
[0.30.2]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.30.2
[0.30.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.30.1
[0.30.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.30.0
[0.29.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.29.0
[0.28.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.28.1
[0.28.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.28.0
[0.27.1]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.27.1
[0.27.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.27.0
[0.26.0]: https://gitlab.com/TIBHannover/orkg/orkg-backend/-/tags/0.26.0
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
