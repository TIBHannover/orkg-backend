package org.orkg.contenttypes.domain.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

fun createDummyComparison() = Comparison(
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
        url = URI.create("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to listOf("0000-0002-1825-0097")
            ),
            homepage = URI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
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
    versions = listOf(
        HeadVersion(
            id = ThingId("R156"),
            label = "Previous version comparison",
            createdAt = OffsetDateTime.parse("2023-04-11T13:15:48.959539600+02:00")
        ),
        HeadVersion(
            id = ThingId("R155"),
            label = "Previous version comparison",
            createdAt = OffsetDateTime.parse("2023-04-10T14:07:21.959539600+02:00")
        )
    ),
    isAnonymized = false,
    visibility = Visibility.DEFAULT
)

fun createDummyComparisonRelatedResource() = ComparisonRelatedResource(
    id = ThingId("R1563"),
    label = "Comparison Related Resource",
    image = "https://example.org/image.png",
    url = "https://orkg.org",
    description = "Description of a Comparison Related Resource",
    createdAt = OffsetDateTime.parse("2023-09-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
)

fun createDummyComparisonRelatedFigure() = ComparisonRelatedFigure(
    id = ThingId("R5476"),
    label = "Comparison Related Figure",
    image = "https://example.org/image.png",
    description = "Description of a Comparison Related Figure",
    createdAt = OffsetDateTime.parse("2023-10-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
)
