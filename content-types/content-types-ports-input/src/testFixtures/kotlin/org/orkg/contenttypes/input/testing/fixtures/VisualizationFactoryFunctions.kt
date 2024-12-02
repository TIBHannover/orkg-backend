package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.input.CreateVisualizationUseCase
import org.orkg.graph.domain.ExtractionMethod

fun dummyCreateVisualizationCommand() = CreateVisualizationUseCase.CreateCommand(
    contributorId = ContributorId("0b3d7108-ea98-448f-85ef-e67a63a8b32b"),
    title = "test",
    description = "visualization description",
    authors = listOf(
        Author(
            id = ThingId("R123"),
            name = "Author with id"
        ),
        Author(
            name = "Author with orcid",
            identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
        ),
        Author(
            id = ThingId("R456"),
            name = "Author with id and orcid",
            identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444"))
        ),
        Author(
            name = "Author with homepage",
            homepage = ParsedIRI("https://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    extractionMethod = ExtractionMethod.UNKNOWN
)
