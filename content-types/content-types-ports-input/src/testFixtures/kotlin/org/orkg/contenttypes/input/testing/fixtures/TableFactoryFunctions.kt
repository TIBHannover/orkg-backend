package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ClassDefinition
import org.orkg.contenttypes.input.CreateTableUseCase
import org.orkg.contenttypes.input.ListDefinition
import org.orkg.contenttypes.input.LiteralDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.contenttypes.input.ResourceDefinition
import org.orkg.contenttypes.input.RowDefinition
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals

fun createTableCommand() = CreateTableUseCase.CreateCommand(
    contributorId = ContributorId("460d4019-57c8-4d30-9087-e7e5129e1c24"),
    label = "Table Title",
    resources = mapOf(
        "#temp1" to ResourceDefinition(
            label = "MOTO",
            classes = setOf(ThingId("Result"))
        )
    ),
    literals = mapOf(
        "#temp2" to LiteralDefinition("column 1", Literals.XSD.STRING.prefixedUri),
        "#temp3" to LiteralDefinition("column 2", Literals.XSD.STRING.prefixedUri),
        "#temp4" to LiteralDefinition("column 3", Literals.XSD.STRING.prefixedUri)
    ),
    predicates = mapOf(
        "#temp5" to PredicateDefinition(
            label = "hasResult",
            description = "has result"
        )
    ),
    lists = mapOf(
        "#temp6" to ListDefinition(
            label = "list",
            elements = listOf("#temp1", "C123")
        )
    ),
    classes = mapOf(
        "#temp7" to ClassDefinition(
            label = "class",
            uri = ParsedIRI("https://orkg.org/class/C1")
        )
    ),
    rows = listOf(
        RowDefinition(
            label = "header",
            data = listOf("#temp1", "#temp2", "#temp3")
        ),
        RowDefinition(
            label = null,
            data = listOf("R456", "#temp4", "#temp5")
        ),
        RowDefinition(
            label = "row 2",
            data = listOf("#temp6", null, "#temp7")
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN
)
