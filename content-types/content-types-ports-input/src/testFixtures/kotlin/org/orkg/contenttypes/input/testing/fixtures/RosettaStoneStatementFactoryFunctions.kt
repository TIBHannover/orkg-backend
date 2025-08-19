package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateRosettaStoneStatementUseCase
import org.orkg.contenttypes.input.UpdateRosettaStoneStatementUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals

fun createRosettaStoneStatementCommand() = CreateRosettaStoneStatementUseCase.CreateCommand(
    templateId = ThingId("R456"),
    contributorId = ContributorId("460d4019-57c8-4d30-9087-e7e5129e1c24"),
    context = ThingId("R789"),
    subjects = listOf("R258", "R369", "#temp1"),
    objects = listOf(
        listOf("R987", "R654", "#temp2", "#temp3"),
        listOf("R321", "R741", "#temp4", "#temp5")
    ),
    certainty = Certainty.HIGH,
    negated = false,
    resources = mapOf(
        "#temp1" to CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("Result"))
        )
    ),
    literals = mapOf(
        "#temp2" to CreateLiteralCommandPart("0.1", Literals.XSD.DECIMAL.prefixedUri)
    ),
    predicates = mapOf(
        "#temp3" to CreatePredicateCommandPart(
            label = "hasResult",
            description = "has result"
        )
    ),
    lists = mapOf(
        "#temp4" to CreateListCommandPart(
            label = "list",
            elements = listOf("#temp1", "C123")
        )
    ),
    classes = mapOf(
        "#temp5" to CreateClassCommandPart(
            label = "class",
            uri = ParsedIRI.create("https://orkg.org/class/C1")
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
    ),
    extractionMethod = ExtractionMethod.MANUAL
)

fun updateRosettaStoneStatementCommand() = UpdateRosettaStoneStatementUseCase.UpdateCommand(
    id = ThingId("R123"),
    contributorId = ContributorId("460d4019-57c8-4d30-9087-e7e5129e1c24"),
    subjects = listOf("R258", "R369", "#temp1"),
    objects = listOf(
        listOf("R987", "R654", "#temp2", "#temp3"),
        listOf("R321", "R741", "#temp4", "#temp5")
    ),
    certainty = Certainty.HIGH,
    negated = false,
    resources = mapOf(
        "#temp1" to CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("Result"))
        )
    ),
    literals = mapOf(
        "#temp2" to CreateLiteralCommandPart("0.1", Literals.XSD.DECIMAL.prefixedUri)
    ),
    predicates = mapOf(
        "#temp3" to CreatePredicateCommandPart(
            label = "hasResult",
            description = "has result"
        )
    ),
    lists = mapOf(
        "#temp4" to CreateListCommandPart(
            label = "list",
            elements = listOf("#temp1", "C123")
        )
    ),
    classes = mapOf(
        "#temp5" to CreateClassCommandPart(
            label = "class",
            uri = ParsedIRI.create("https://orkg.org/class/C1")
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
    ),
    extractionMethod = ExtractionMethod.MANUAL
)
