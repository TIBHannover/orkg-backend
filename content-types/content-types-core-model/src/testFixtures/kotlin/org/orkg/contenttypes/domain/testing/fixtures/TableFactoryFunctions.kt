package org.orkg.contenttypes.domain.testing.fixtures

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createResource

fun createTable() = Table(
    id = ThingId("R4517"),
    label = "Table Title",
    rows = listOf(
        createTableRow(
            label = null,
            data = listOf(
                createLiteral(label = "Column 1"),
                createLiteral(label = "Column 2"),
                createLiteral(label = "Column 3")
            )
        ),
        createTableRow("Row 1"),
        createTableRow("Row 2"),
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT
)

fun createTableRow(
    label: String? = "Row 1",
    data: List<Thing?> = listOf(
        createClass(),
        createLiteral(),
        createResource()
    )
) = Table.Row(label, data)
