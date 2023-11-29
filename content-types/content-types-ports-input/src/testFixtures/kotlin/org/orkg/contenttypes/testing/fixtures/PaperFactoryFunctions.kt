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
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

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
