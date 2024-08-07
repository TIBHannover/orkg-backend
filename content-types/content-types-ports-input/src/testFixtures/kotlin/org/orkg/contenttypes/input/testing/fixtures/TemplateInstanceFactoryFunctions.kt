package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.UpdateTemplateInstanceUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals

fun dummyUpdateTemplateInstanceCommand() = UpdateTemplateInstanceUseCase.UpdateCommand(
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
    lists = mapOf(
        "#temp5" to ListDefinition(
            label = "list",
            elements = listOf("R465")
        )
    ),
    classes = mapOf(
        "#temp6" to ClassDefinition(
            label = "some class",
            uri = ParsedIRI("https://orkg.org/C1")
        )
    ),
    extractionMethod = ExtractionMethod.MANUAL
)
