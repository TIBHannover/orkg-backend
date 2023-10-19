package eu.tib.orkg.prototype.contenttypes.testing.fixtures

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

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

fun dummyCreatePaperCommand() = CreatePaperUseCase.CreateCommand(
    contributorId = ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"),
    title = "test",
    researchFields = listOf(ThingId("R12")),
    identifiers = mapOf("doi" to "10.1234/56789"),
    publicationInfo = PublicationInfo(
        publishedYear = 2015,
        publishedMonth = 5,
        publishedIn = "conference",
        url = URI.create("http://example.org")
    ),
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
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    contents = CreatePaperUseCase.CreateCommand.PaperContents(
        resources = mapOf(
            "#temp1" to CreatePaperUseCase.CreateCommand.ResourceDefinition(
                label = "MOTO",
                classes = setOf(ThingId("R2000"))
            )
        ),
        literals = mapOf(
            "#temp2" to CreatePaperUseCase.CreateCommand.LiteralDefinition(
                label = "0.1",
                dataType = Literals.XSD.DECIMAL.prefixedUri
            )
        ),
        predicates = mapOf(
            "#temp3" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
                label = "hasResult",
                description = "has result"
            ),
            "#temp4" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
                label = "hasLiteral"
            )
        ),
        contributions = listOf(
            CreatePaperUseCase.CreateCommand.Contribution(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1")
                    )
                )
            ),
            CreatePaperUseCase.CreateCommand.Contribution(
                label = "Contribution 2",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1"),
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"),
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp2")
                                ),
                                "#temp4" to listOf(
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1")
                                )
                            )
                        )
                    )
                )
            )
        )
    ),
    extractionMethod = ExtractionMethod.MANUAL
)
