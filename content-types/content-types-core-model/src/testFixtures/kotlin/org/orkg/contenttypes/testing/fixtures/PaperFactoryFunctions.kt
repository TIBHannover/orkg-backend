package org.orkg.contenttypes.testing.fixtures

import java.net.URI
import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility

fun createDummyPaper() = Paper(
    id = ThingId("R8186"),
    title = "Dummy Paper Title",
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
        "doi" to "10.1000/182"
    ),
    publicationInfo = PublicationInfo(
        publishedMonth = 4,
        publishedYear = 2023,
        publishedIn = "Fancy Conference",
        url = URI.create("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to "0000-0002-1825-0097"
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
    visibility = Visibility.DEFAULT,
    verified = false
)
