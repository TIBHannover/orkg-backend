package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonConfig
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonData
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.PublishComparisonUseCase
import org.orkg.contenttypes.input.UpdateComparisonUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Visibility
import java.util.UUID

fun createComparisonCommand() = CreateComparisonUseCase.CreateCommand(
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
    sustainableDevelopmentGoals = setOf(ThingId("SDG_1"), ThingId("SDG_2")),
    contributions = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120")),
    config = createComparisonConfig(),
    data = createComparisonData(),
    visualizations = listOf(ThingId("R63845")),
    references = listOf("https://orkg.org/resources/R1000", "paper citation"),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    isAnonymized = false,
    extractionMethod = ExtractionMethod.UNKNOWN
)

fun updateComparisonCommand() = UpdateComparisonUseCase.UpdateCommand(
    comparisonId = ThingId("R16453"),
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
    sustainableDevelopmentGoals = setOf(ThingId("SDG_2"), ThingId("SDG_3")),
    contributions = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120")),
    config = createComparisonConfig(),
    data = createComparisonData(),
    visualizations = listOf(ThingId("R63845")),
    references = listOf("https://orkg.org/resources/R1000", "paper citation"),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    isAnonymized = false,
    extractionMethod = ExtractionMethod.UNKNOWN,
    visibility = Visibility.DEFAULT
)

fun publishComparisonCommand() = PublishComparisonUseCase.PublishCommand(
    id = ThingId("R16453"),
    contributorId = ContributorId(UUID.randomUUID()),
    subject = "Research Field 1",
    description = "comparison description",
    authors = listOf(
        Author(
            id = null,
            name = "Author 1",
            identifiers = emptyMap(),
            homepage = null
        ),
        Author(
            id = ThingId("R132564"),
            name = "Author 2",
            identifiers = mapOf(
                "orcid" to listOf("0000-1111-2222-3333")
            ),
            homepage = ParsedIRI("https://example.org")
        )
    ),
    assignDOI = true
)
