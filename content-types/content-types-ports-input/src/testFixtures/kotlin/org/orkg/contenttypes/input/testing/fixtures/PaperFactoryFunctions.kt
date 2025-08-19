package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.contenttypes.input.UpdatePaperUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility

fun createPaperCommand() = CreatePaperUseCase.CreateCommand(
    contributorId = ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"),
    title = "test",
    researchFields = listOf(ThingId("R12")),
    identifiers = mapOf("doi" to listOf("10.1234/56789")),
    publicationInfo = PublicationInfoCommand(
        publishedYear = 2015,
        publishedMonth = 5,
        publishedIn = "conference",
        url = ParsedIRI.create("https://example.org")
    ),
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
            homepage = ParsedIRI.create("https://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    sustainableDevelopmentGoals = setOf(ThingId("SDG_1"), ThingId("SDG_2")),
    mentionings = setOf(ThingId("R159"), ThingId("R753")),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    contents = CreatePaperUseCase.CreateCommand.PaperContents(
        resources = mapOf(
            "#temp1" to CreateResourceCommandPart(
                label = "MOTO",
                classes = setOf(ThingId("R2000"))
            )
        ),
        literals = mapOf(
            "#temp2" to CreateLiteralCommandPart(
                label = "0.1",
                dataType = Literals.XSD.DECIMAL.prefixedUri
            )
        ),
        predicates = mapOf(
            "#temp3" to CreatePredicateCommandPart(
                label = "hasResult",
                description = "has result"
            ),
            "#temp4" to CreatePredicateCommandPart(
                label = "hasLiteral"
            )
        ),
        contributions = listOf(
            CreateContributionCommandPart(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreateContributionCommandPart.StatementObject("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreateContributionCommandPart.StatementObject("#temp1")
                    )
                )
            ),
            CreateContributionCommandPart(
                label = "Contribution 2",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreateContributionCommandPart.StatementObject("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreateContributionCommandPart.StatementObject("#temp1"),
                        CreateContributionCommandPart.StatementObject(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    CreateContributionCommandPart.StatementObject("R3003"),
                                    CreateContributionCommandPart.StatementObject("#temp2")
                                ),
                                "#temp4" to listOf(
                                    CreateContributionCommandPart.StatementObject("#temp1")
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

fun updatePaperCommand() = UpdatePaperUseCase.UpdateCommand(
    paperId = ThingId("R001"),
    contributorId = ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"),
    title = "test",
    researchFields = listOf(ThingId("R12")),
    identifiers = mapOf("doi" to listOf("10.1234/56789")),
    publicationInfo = PublicationInfoCommand(
        publishedYear = 2015,
        publishedMonth = 5,
        publishedIn = "conference",
        url = ParsedIRI.create("https://example.org")
    ),
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
            homepage = ParsedIRI.create("https://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    sustainableDevelopmentGoals = setOf(
        ThingId("SDG_3"),
        ThingId("SDG_4")
    ),
    mentionings = setOf(
        ThingId("R591"),
        ThingId("R357")
    ),
    observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
    organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
    extractionMethod = ExtractionMethod.AUTOMATIC,
    visibility = Visibility.DEFAULT,
    verified = false
)

fun publishPaperCommand() = PublishPaperUseCase.PublishCommand(
    id = ThingId("R123"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    subject = "subject of the paper",
    description = "review about important topic",
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
            homepage = ParsedIRI.create("https://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    )
)
