package org.orkg.contenttypes.domain.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Visualization
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

fun createVisualization() = Visualization(
    id = ThingId("R8186"),
    title = "Dummy Visualization Title",
    description = "Some description about the contents",
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
    visibility = Visibility.DEFAULT
)
