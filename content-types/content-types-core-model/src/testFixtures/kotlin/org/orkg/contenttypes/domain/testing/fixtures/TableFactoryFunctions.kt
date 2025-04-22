package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.time.OffsetDateTime
import java.util.Base64
import kotlin.math.absoluteValue
import kotlin.random.Random

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
    visibility = Visibility.DEFAULT,
    modifiable = true,
)

fun createTableRow(
    label: String? = "Row 1",
    data: List<Thing?> = listOf(
        createClass(),
        createLiteral(),
        createResource()
    ),
) = Table.Row(label, data)

fun createTableStatements(
    tableId: ThingId,
    rowCount: Int,
    columnCount: Int,
    valueIds: List<List<ThingId?>?>? = null,
    random: Random = Random(1234567890),
): List<GeneralStatement> {
    require(rowCount >= 1) { "Row count must be at least 1." }
    require(columnCount >= 1) { "Column count must be at least 1." }
    if (valueIds != null) {
        if (valueIds.size != rowCount.toInt()) {
            throw IllegalStateException("""Size of valueIds must match size rowCount.""")
        }
        valueIds.forEach { row ->
            if (row != null && row.size != columnCount.toInt()) {
                throw IllegalStateException("""Size of each entry in valueIds must match columnCount.""")
            }
        }
    }
    val table = createResource(tableId, classes = setOf(Classes.table))
    val hasRow = createPredicate(Predicates.csvwRows)
    val hasNumber = createPredicate(Predicates.csvwNumber)
    val hasTitle = createPredicate(Predicates.csvwTitles)
    val hasCell = createPredicate(Predicates.csvwCells)
    val hasColumn = createPredicate(Predicates.csvwColumn)
    val hasColumns = createPredicate(Predicates.csvwColumns)
    val hasValue = createPredicate(Predicates.csvwValue)
    val columns = (0 until columnCount.toInt()).map { columnIndex ->
        createResource(ThingId("Column_${columnIndex + 1}"), classes = setOf(Classes.column))
    }
    val columnStatements = columns.flatMapIndexed { columnIndex, column ->
        val number = createLiteral(
            id = ThingId("Column_${columnIndex + 1}_Number"),
            label = (columnIndex + 1).toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val title = createLiteral(
            id = ThingId("Column_${columnIndex + 1}_Title"),
            label = Base64.getEncoder().encodeToString(random.nextBytes(16))
        )
        listOf(
            createStatement(
                id = StatementId("S_${table.id}--${hasColumns.id}--${column.id}"),
                subject = table,
                predicate = hasColumns,
                `object` = column
            ),
            createStatement(
                id = StatementId("S_${column.id}--${hasNumber.id}--${number.id}"),
                subject = column,
                predicate = hasNumber,
                `object` = number
            ),
            createStatement(
                id = StatementId("S_${column.id}--${hasTitle.id}--${title.id}"),
                subject = column,
                predicate = hasTitle,
                `object` = title
            ),
        )
    }
    val rowStatements = (0 until rowCount.toInt()).flatMap { rowIndex ->
        val row = createResource(ThingId("Row_${rowIndex + 1}"), classes = setOf(Classes.row))
        val number = createLiteral(
            id = ThingId("Row_${rowIndex + 1}_Number"),
            label = (rowIndex + 1).toString(),
            datatype = Literals.XSD.INT.prefixedUri
        )
        val title = createLiteral(
            id = ThingId("Row_${rowIndex + 1}_Title"),
            label = Base64.getEncoder().encodeToString(random.nextBytes(16))
        )
        listOf(
            createStatement(
                id = StatementId("S_${table.id}--${hasRow.id}--${row.id}"),
                subject = table,
                predicate = hasRow,
                `object` = row
            ),
            createStatement(
                id = StatementId("S_${row.id}--${hasNumber.id}--${number.id}"),
                subject = row,
                predicate = hasNumber,
                `object` = number
            ),
            createStatement(
                id = StatementId("S_${row.id}--${hasTitle.id}--${title.id}"),
                subject = row,
                predicate = hasTitle,
                `object` = title
            ),
            *(0 until columnCount.toInt()).flatMap { columnIndex ->
                val cell = createResource(
                    id = ThingId("Cell_${rowIndex + 1}_${columnIndex + 1}"),
                    classes = setOf(Classes.cell)
                )
                val valueId = valueIds?.get(rowIndex)?.get(columnIndex) ?: ThingId("R${random.nextLong().absoluteValue}")
                val value = createResource(valueId)
                listOf(
                    createStatement(
                        id = StatementId("S_${row.id}--${hasCell.id}--${cell.id}"),
                        subject = row,
                        predicate = hasCell,
                        `object` = cell
                    ),
                    createStatement(
                        id = StatementId("S_${cell.id}--${hasColumn.id}--${columns[columnIndex].id}"),
                        subject = cell,
                        predicate = hasColumn,
                        `object` = columns[columnIndex]
                    ),
                    createStatement(
                        id = StatementId("S_${cell.id}--${hasValue.id}--${value.id}"),
                        subject = cell,
                        predicate = hasValue,
                        `object` = value
                    )
                )
            }.toTypedArray()
        )
    }
    return rowStatements + columnStatements
}
