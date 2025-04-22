package org.orkg.contenttypes.input.testing.fixtures

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateClassCommandPart
import org.orkg.contenttypes.input.CreateListCommandPart
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.CreateResourceCommandPart
import org.orkg.contenttypes.input.CreateTableUseCase
import org.orkg.contenttypes.input.RowCommand
import org.orkg.contenttypes.input.UpdateTableUseCase
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Visibility

fun createTableCommand() = CreateTableUseCase.CreateCommand(
    contributorId = ContributorId("460d4019-57c8-4d30-9087-e7e5129e1c24"),
    label = "Table Title",
    resources = mapOf(
        "#temp1" to CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("Result"))
        )
    ),
    literals = mapOf(
        "#temp2" to CreateLiteralCommandPart("column 1", Literals.XSD.STRING.prefixedUri),
        "#temp3" to CreateLiteralCommandPart("column 2", Literals.XSD.STRING.prefixedUri),
        "#temp4" to CreateLiteralCommandPart("column 3", Literals.XSD.STRING.prefixedUri)
    ),
    predicates = mapOf(
        "#temp5" to CreatePredicateCommandPart(
            label = "hasResult",
            description = "has result"
        )
    ),
    lists = mapOf(
        "#temp6" to CreateListCommandPart(
            label = "list",
            elements = listOf("#temp1", "C123")
        )
    ),
    classes = mapOf(
        "#temp7" to CreateClassCommandPart(
            label = "class",
            uri = ParsedIRI("https://orkg.org/class/C1")
        )
    ),
    rows = listOf(
        RowCommand(
            label = "header",
            data = listOf("#temp1", "#temp2", "#temp3")
        ),
        RowCommand(
            label = null,
            data = listOf("R456", "#temp4", "#temp5")
        ),
        RowCommand(
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

fun updateTableCommand() = UpdateTableUseCase.UpdateCommand(
    tableId = ThingId("R123"),
    contributorId = ContributorId("460d4019-57c8-4d30-9087-e7e5129e1c24"),
    label = "Table Title",
    resources = mapOf(
        "#temp1" to CreateResourceCommandPart(
            label = "MOTO",
            classes = setOf(ThingId("Result"))
        )
    ),
    literals = mapOf(
        "#temp2" to CreateLiteralCommandPart("column 1", Literals.XSD.STRING.prefixedUri),
        "#temp3" to CreateLiteralCommandPart("column 2", Literals.XSD.STRING.prefixedUri),
        "#temp4" to CreateLiteralCommandPart("column 3", Literals.XSD.STRING.prefixedUri)
    ),
    predicates = mapOf(
        "#temp5" to CreatePredicateCommandPart(
            label = "hasResult",
            description = "has result"
        )
    ),
    lists = mapOf(
        "#temp6" to CreateListCommandPart(
            label = "list",
            elements = listOf("#temp1", "C123")
        )
    ),
    classes = mapOf(
        "#temp7" to CreateClassCommandPart(
            label = "class",
            uri = ParsedIRI("https://orkg.org/class/C1")
        )
    ),
    rows = listOf(
        RowCommand(
            label = "header",
            data = listOf("#temp1", "#temp2", "#temp3")
        ),
        RowCommand(
            label = null,
            data = listOf("R456", "#temp4", "#temp5")
        ),
        RowCommand(
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
    extractionMethod = ExtractionMethod.UNKNOWN,
    visibility = Visibility.DEFAULT,
)
