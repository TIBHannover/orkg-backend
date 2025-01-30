package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonData
import org.orkg.contenttypes.domain.ComparisonHeaderCell
import org.orkg.contenttypes.domain.ComparisonIndexCell
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ComparisonType
import org.orkg.contenttypes.domain.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.contenttypes.domain.PublishedVersion
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.domain.PublishedComparison
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

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
        url = ParsedIRI("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            ),
            homepage = ParsedIRI("https://example.org")
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
    contributions = listOf(
        ObjectIdAndLabel(
            id = ThingId("R258"),
            label = "Contribution 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R396"),
            label = "Contribution 2"
        )
    ),
    config = createComparisonConfig(),
    data = createComparisonData(),
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

fun createPublishedComparison(): PublishedComparison =
    PublishedComparison(
        id = ThingId("R5476"),
        config = createComparisonConfig(),
        data = createComparisonData()
    )

fun createComparisonTable(): ComparisonTable =
    ComparisonTable(
        id = ThingId("R8186"),
        config = createComparisonConfig(),
        data = createComparisonData()
    )

fun createComparisonConfig(): ComparisonConfig =
    ComparisonConfig(
        predicates = listOf(),
        contributions = listOf("R456789", "R987654"),
        transpose = false,
        type = ComparisonType.MERGE,
        shortCodes = emptyList()
    )

fun createComparisonData(): ComparisonData =
    ComparisonData(
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
                        path = listOf(ThingId("R187004"), ThingId("P32")),
                        pathLabels = listOf("Contribution 1", "research problem"),
                        `class` = "resource"
                    )
                )
            )
        )
    )
