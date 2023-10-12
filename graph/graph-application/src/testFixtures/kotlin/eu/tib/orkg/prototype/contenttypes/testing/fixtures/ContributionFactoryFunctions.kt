package eu.tib.orkg.prototype.contenttypes.testing.fixtures

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.util.*

fun createDummyContribution() = Contribution(
    id = ThingId("R15634"),
    label = "Contribution",
    classes = setOf(ThingId("C123")),
    properties = mapOf(
        Predicates.hasEvaluation to listOf(ThingId("R123"))
    ),
    visibility = Visibility.DEFAULT
)

fun dummyCreateContributionCommand() = CreateContributionUseCase.CreateCommand(
    contributorId = ContributorId(UUID.fromString("5342d678-c54a-45ec-bc79-977c79dfff7d")),
    paperId = ThingId("R123"),
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
    contribution = CreatePaperUseCase.CreateCommand.Contribution(
        label = "Contribution 1",
        classes = setOf(ThingId("C123")),
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
