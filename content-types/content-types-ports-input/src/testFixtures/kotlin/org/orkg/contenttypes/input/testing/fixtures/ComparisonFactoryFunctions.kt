package org.orkg.contenttypes.input.testing.fixtures

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.graph.domain.ExtractionMethod

fun dummyCreateComparisonCommand() = CreateComparisonUseCase.CreateCommand(
    contributorId = ContributorId("0b3d7108-ea98-448f-85ef-e67a63a8b32b"),
    title = "test",
    description = "comparison description",
    researchFields = listOf(ThingId("R12")),
    authors = listOf(
        Author(
            id = ThingId("R123"),
            name = "Author with id"
        ),
        Author(
            name = "Author with orcid",
            identifiers = mapOf("orcid" to "0000-1111-2222-3333")
        ),
        Author(
            id = ThingId("R456"),
            name = "Author with id and orcid",
            identifiers = mapOf("orcid" to "1111-2222-3333-4444")
        ),
        Author(
            name = "Author with homepage",
            homepage = URI.create("http://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    contributions = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120")),
    references = listOf("https://orkg.org/resources/R1000", "paper citation"),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    isAnonymized = false,
    extractionMethod = ExtractionMethod.UNKNOWN
)
