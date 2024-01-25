package org.orkg.contenttypes.input.testing.fixtures

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates

fun dummyCreateContributionCommand() = CreateContributionUseCase.CreateCommand(
    contributorId = ContributorId(UUID.fromString("5342d678-c54a-45ec-bc79-977c79dfff7d")),
    paperId = ThingId("R123"),
    extractionMethod = ExtractionMethod.MANUAL,
    resources = mapOf(
        "#temp1" to ResourceDefinition(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
    ),
    literals = mapOf(
        "#temp2" to LiteralDefinition(
            label = "0.1",
            dataType = Literals.XSD.DECIMAL.prefixedUri
        )
    ),
    predicates = mapOf(
        "#temp3" to PredicateDefinition(
            label = "hasResult",
            description = "has result"
        ),
        "#temp4" to PredicateDefinition(
            label = "hasLiteral"
        )
    ),
    contribution = ContributionDefinition(
        label = "Contribution 1",
        classes = setOf(ThingId("C123")),
        statements = mapOf(
            Predicates.hasResearchProblem.value to listOf(
                ContributionDefinition.StatementObjectDefinition("R3003")
            ),
            Predicates.hasEvaluation.value to listOf(
                ContributionDefinition.StatementObjectDefinition("#temp1"),
                ContributionDefinition.StatementObjectDefinition(
                    id = "R3004",
                    statements = mapOf(
                        "#temp3" to listOf(
                            ContributionDefinition.StatementObjectDefinition("R3003"),
                            ContributionDefinition.StatementObjectDefinition("#temp2")
                        ),
                        "#temp4" to listOf(
                            ContributionDefinition.StatementObjectDefinition("#temp1")
                        )
                    )
                )
            )
        )
    )
)
