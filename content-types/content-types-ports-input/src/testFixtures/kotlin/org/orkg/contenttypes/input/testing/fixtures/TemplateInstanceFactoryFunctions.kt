package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.UpdateTemplateInstanceUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals

fun updateTemplateInstanceCommand() = UpdateTemplateInstanceUseCase.UpdateCommand(
    subject = ThingId("R123"),
    templateId = ThingId("R456"),
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    statements = mapOf(
        ThingId("P1") to listOf(
            "#temp1",
            "#temp2",
            "#temp3",
            "#temp4",
            "#temp5",
            "#temp6",
            "R789"
        )
    ),
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
    lists = mapOf(
        "#temp5" to CreateListCommandPart(
            label = "list",
            elements = listOf("R465")
        )
    ),
    classes = mapOf(
        "#temp6" to CreateClassCommandPart(
            label = "some class",
            uri = ParsedIRI.create("https://orkg.org/C1")
        )
    ),
    extractionMethod = ExtractionMethod.MANUAL
)
