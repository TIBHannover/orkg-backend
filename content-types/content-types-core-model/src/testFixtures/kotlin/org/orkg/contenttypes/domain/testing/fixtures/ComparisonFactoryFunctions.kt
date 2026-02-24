package org.orkg.contenttypes.domain.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonColumnData
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonTableValue
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.domain.PublishedVersion
import org.orkg.contenttypes.domain.SimpleComparisonPath
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.domain.legacy.ComparisonHeaderCell
import org.orkg.contenttypes.domain.legacy.ComparisonIndexCell
import org.orkg.contenttypes.domain.legacy.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.legacy.LegacyComparisonConfig
import org.orkg.contenttypes.domain.legacy.LegacyComparisonData
import org.orkg.contenttypes.domain.legacy.LegacyComparisonType
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.time.OffsetDateTime

fun createComparison() = Comparison(
    id = ThingId("R8186"),
    title = "Dummy Comparison Title",
    description = "Some description about the contents",
    researchFields = listOf(
        ObjectIdAndLabel(
            id = ThingId("R456"),
            label = "Research Field 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R789"),
            label = "Research Field 2"
        )
    ),
    identifiers = mapOf(
        "doi" to listOf("10.1000/182")
    ),
    publicationInfo = PublicationInfo(
        publishedMonth = 4,
        publishedYear = 2023,
        publishedIn = ObjectIdAndLabel(ThingId("R4867"), "ORKG"),
        url = ParsedIRI.create("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            ),
            homepage = ParsedIRI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
        )
    ),
    sustainableDevelopmentGoals = setOf(
        ObjectIdAndLabel(
            id = ThingId("SDG_1"),
            label = "No poverty"
        ),
        ObjectIdAndLabel(
            id = ThingId("SDG_2"),
            label = "Zero hunger"
        )
    ),
    sources = listOf(
        ComparisonDataSource(
            id = ThingId("R258"),
            type = ComparisonDataSource.Type.THING
        ),
        ComparisonDataSource(
            id = ThingId("R396"),
            type = ComparisonDataSource.Type.THING
        )
    ),
    visualizations = listOf(
        ObjectIdAndLabel(
            id = ThingId("R159"),
            label = "Visualization 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R357"),
            label = "Visualization 2"
        )
    ),
    relatedFigures = listOf(
        ObjectIdAndLabel(
            id = ThingId("R951"),
            label = "Related Figure 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R753"),
            label = "Related Figure 2"
        )
    ),
    relatedResources = listOf(
        ObjectIdAndLabel(
            id = ThingId("R741"),
            label = "Related Resource 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R852"),
            label = "Related Resource 2"
        )
    ),
    references = listOf(
        "https://www.reference.com/1",
        "https://www.reference.com/2"
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    versions = VersionInfo(
        head = HeadVersion(
            id = ThingId("R8186"),
            label = "Dummy Comparison Title",
            createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        ),
        published = listOf(
            PublishedVersion(
                id = ThingId("R156"),
                label = "Previous version comparison",
                createdAt = OffsetDateTime.parse("2023-04-11T13:15:48.959539600+02:00"),
                createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                changelog = null
            ),
            PublishedVersion(
                id = ThingId("R155"),
                label = "Previous version comparison",
                createdAt = OffsetDateTime.parse("2023-04-10T14:07:21.959539600+02:00"),
                createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                changelog = null
            )
        )
    ),
    isAnonymized = false,
    published = false,
    visibility = Visibility.DEFAULT
)

fun createComparisonRelatedResource() = ComparisonRelatedResource(
    id = ThingId("R1563"),
    label = "Comparison Related Resource",
    image = "https://example.org/image.png",
    url = "https://orkg.org",
    description = "Description of a Comparison Related Resource",
    createdAt = OffsetDateTime.parse("2023-09-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
)

fun createComparisonRelatedFigure() = ComparisonRelatedFigure(
    id = ThingId("R5476"),
    label = "Comparison Related Figure",
    image = "https://example.org/image.png",
    description = "Description of a Comparison Related Figure",
    createdAt = OffsetDateTime.parse("2023-10-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
)

fun createSimpleComparisonPaths() = listOf(
    SimpleComparisonPath(
        id = Predicates.addresses,
        type = ComparisonPath.Type.PREDICATE,
        children = emptyList(),
    ),
    SimpleComparisonPath(
        id = Predicates.mentions,
        type = ComparisonPath.Type.PREDICATE,
        children = listOf(
            SimpleComparisonPath(
                id = Predicates.description,
                type = ComparisonPath.Type.PREDICATE,
                children = emptyList(),
            )
        ),
    ),
    SimpleComparisonPath(
        id = ThingId("R21325"),
        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
        children = listOf(
            SimpleComparisonPath(
                id = Predicates.hasSubjectPosition,
                type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                children = emptyList(),
            ),
            SimpleComparisonPath(
                id = ThingId("hasObjectPosition1"),
                type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                children = emptyList(),
            ),
            SimpleComparisonPath(
                id = ThingId("hasObjectPosition2"),
                type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                children = emptyList(),
            )
        ),
    ),
)

fun createLabeledComparisonPaths(): List<LabeledComparisonPath> =
    listOf(
        LabeledComparisonPath(
            id = Predicates.addresses,
            label = "addresses",
            description = "addresses",
            type = ComparisonPath.Type.PREDICATE,
            children = emptyList(),
        ),
        LabeledComparisonPath(
            id = Predicates.mentions,
            label = "mentions",
            description = null,
            type = ComparisonPath.Type.PREDICATE,
            children = listOf(
                LabeledComparisonPath(
                    id = Predicates.description,
                    label = "description",
                    description = "description",
                    type = ComparisonPath.Type.PREDICATE,
                    children = emptyList(),
                )
            ),
        ),
        LabeledComparisonPath(
            id = ThingId("R21325"),
            label = "Dummy Rosetta Stone Template Label",
            description = "Some description about the rosetta stone template",
            type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
            children = listOf(
                LabeledComparisonPath(
                    id = Predicates.hasSubjectPosition,
                    label = "resource property placeholder",
                    description = "resource property description",
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                    children = emptyList(),
                ),
                LabeledComparisonPath(
                    id = ThingId("hasObjectPosition1"),
                    label = "string property placeholder",
                    description = "string literal property description",
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                    children = emptyList(),
                ),
                LabeledComparisonPath(
                    id = ThingId("hasObjectPosition2"),
                    label = "number literal property placeholder",
                    description = "number literal property description",
                    type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                    children = emptyList(),
                )
            ),
        ),
    )

fun createComparisonTable(): ComparisonTable =
    ComparisonTable.from(
        comparisonId = ThingId("R5476"),
        selectedPaths = createLabeledComparisonPaths(),
        columnData = createComparisonColumnData()
    )

fun createComparisonColumnData(): List<ComparisonColumnData> =
    listOf(
        ComparisonColumnData(
            title = createResource(id = ThingId("R396"), label = "Title 1"),
            subtitle = createResource(id = ThingId("R259"), label = "Contribution 1"),
            values = mapOf(
                Predicates.addresses to listOf(
                    ComparisonTableValue(
                        value = createResource(id = ThingId("R260"), label = "Some research problem"),
                        children = emptyMap()
                    )
                ),
                Predicates.mentions to listOf(
                    ComparisonTableValue(
                        value = createResource(id = ThingId("R261"), label = "Some resource"),
                        children = emptyMap()
                    ),
                    ComparisonTableValue(
                        value = createPredicate(id = ThingId("P456"), label = "Some predicate"),
                        children = mapOf(
                            Predicates.description to listOf(
                                ComparisonTableValue(
                                    value = createLiteral(id = ThingId("L126"), label = "Predicate description"),
                                    children = emptyMap()
                                ),
                            )
                        )
                    )
                ),
            )
        ),
        ComparisonColumnData(
            title = createResource(id = ThingId("R258"), label = "Title 2"),
            subtitle = null,
            values = mapOf(
                Predicates.addresses to listOf(
                    ComparisonTableValue(
                        value = createResource(id = ThingId("R260"), label = "Some research problem"),
                        children = emptyMap()
                    )
                ),
                Predicates.mentions to listOf(
                    ComparisonTableValue(
                        value = createResource(id = ThingId("R354"), label = "Some Resource"),
                        children = emptyMap()
                    ),
                    ComparisonTableValue(
                        value = createPredicate(id = ThingId("P476"), label = "Some other predicate"),
                        children = mapOf(
                            Predicates.description to listOf(
                                ComparisonTableValue(
                                    value = createLiteral(id = ThingId("L146"), label = "Predicate description"),
                                    children = emptyMap()
                                ),
                            )
                        )
                    )
                ),
                Predicates.mentions to listOf(
                    ComparisonTableValue(
                        value = createResource(id = ThingId("R261"), label = "Some Resource"),
                        children = emptyMap()
                    ),
                    ComparisonTableValue(
                        value = createPredicate(id = ThingId("P456"), label = "Some Predicate"),
                        children = mapOf(
                            Predicates.description to listOf(
                                ComparisonTableValue(
                                    value = createLiteral(id = ThingId("L126"), label = "Predicate description"),
                                    children = emptyMap()
                                ),
                            )
                        )
                    )
                ),
            )
        ),
        ComparisonColumnData(
            title = createResource(
                id = ThingId("R021"),
                label = "Default Label",
                classes = setOf(Classes.paper),
            ),
            subtitle = null,
            values = mapOf(
                ThingId("R21325") to listOf(
                    ComparisonTableValue(
                        value = createResource(
                            id = ThingId("R147"),
                            label = "Subject 1 and Subject 2 Object 1-1 and Object 1-2 Object 2-1 and Object 2-2",
                            classes = setOf(Classes.rosettaStoneStatement, Classes.latestVersion, ThingId("R321")),
                            createdAt = OffsetDateTime.parse("2024-04-30T16:22:58.959539600+02:00"),
                            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                            observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
                            organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
                            extractionMethod = ExtractionMethod.MANUAL,
                        ),
                        children = mapOf(
                            Predicates.hasSubjectPosition to listOf(
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R258"), label = "Subject 1"),
                                    children = emptyMap()
                                ),
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R369"), label = "Subject 2"),
                                    children = emptyMap()
                                ),
                            ),
                            ThingId("hasObjectPosition1") to listOf(
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R987"), label = "Object 1-1"),
                                    children = emptyMap()
                                ),
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R654"), label = "Object 1-2"),
                                    children = emptyMap()
                                ),
                            ),
                            ThingId("hasObjectPosition2") to listOf(
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R321"), label = "Object 2-1"),
                                    children = emptyMap()
                                ),
                                ComparisonTableValue(
                                    value = createResource(id = ThingId("R741"), label = "Object 2-2"),
                                    children = emptyMap()
                                ),
                            )
                        )
                    )
                ),
            )
        ),
    )

// Legacy factory funtcions:

fun createComparisonConfig(): LegacyComparisonConfig =
    LegacyComparisonConfig(
        predicates = listOf(),
        contributions = listOf("R456789", "R987654"),
        transpose = false,
        type = LegacyComparisonType.MERGE,
        shortCodes = emptyList()
    )

fun createComparisonData(): LegacyComparisonData =
    LegacyComparisonData(
        listOf(
            ComparisonHeaderCell(
                id = "R456789",
                label = "Contribution 1",
                paperId = "R456",
                paperLabel = "Paper 1",
                paperYear = 2024,
                active = true
            ),
            ComparisonHeaderCell(
                id = "R987654",
                label = "Contribution 1",
                paperId = "R789",
                paperLabel = "Paper 2",
                paperYear = 2022,
                active = true
            )
        ),
        listOf(
            ComparisonIndexCell(
                id = "P32",
                label = "research problem",
                contributionAmount = 2,
                active = true,
                similarPredicates = listOf("P15")
            )
        ),
        mapOf(
            "P32" to listOf(
                listOf(
                    ConfiguredComparisonTargetCell(
                        id = "R192326",
                        label = "Covid-19 Pandemic Ontology Development",
                        classes = listOf(Classes.problem),
                        path = listOf(ThingId("R187004"), Predicates.hasResearchProblem),
                        pathLabels = listOf("Contribution 1", "research problem"),
                        `class` = "resource"
                    )
                )
            )
        )
    )
