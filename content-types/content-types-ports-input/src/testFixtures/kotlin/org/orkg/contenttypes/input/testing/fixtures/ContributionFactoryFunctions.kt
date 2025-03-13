package org.orkg.contenttypes.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import java.util.UUID

fun createContributionCommand() = CreateContributionUseCase.CreateCommand(
    contributorId = ContributorId(UUID.fromString("5342d678-c54a-45ec-bc79-977c79dfff7d")),
    paperId = ThingId("R123"),
    extractionMethod = ExtractionMethod.MANUAL,
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
    contribution = CreateContributionCommandPart(
        label = "Contribution 1",
        classes = setOf(ThingId("C123")),
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
