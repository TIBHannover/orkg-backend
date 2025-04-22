package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.CellGraph
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.ColumnGraph
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State.RowGraph
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

fun createRowGraph(id: ThingId, cellValues: List<Thing?>): RowGraph {
    val row = createResource(id, classes = setOf(Classes.row))
    val table = createResource(ThingId("Table"), classes = setOf(Classes.table))
    val hasRow = createPredicate(Predicates.csvwRows)
    val hasTitle = createPredicate(Predicates.csvwTitles)
    val hasNumber = createPredicate(Predicates.csvwNumber)
    val hasCell = createPredicate(Predicates.csvwCells)
    val hasValue = createPredicate(Predicates.csvwValue)
    val hasColumn = createPredicate(Predicates.csvwColumn)
    val title = createLiteral(ThingId("${id}_Title"), label = "Row $id Label")
    val number = createLiteral(ThingId("${id}_Number"), label = "1", datatype = Literals.XSD.INT.prefixedUri)
    return RowGraph(
        tableHasRowStatement = createStatement(
            id = StatementId("S_${table.id}--${hasRow.id}--${row.id}"),
            subject = table,
            predicate = hasRow,
            `object` = row
        ),
        labelStatement = createStatement(
            id = StatementId("S_${row.id}--${hasTitle.id}--${title.id}"),
            subject = row,
            predicate = hasTitle,
            `object` = title
        ),
        indexStatement = createStatement(
            id = StatementId("S_${row.id}--${hasNumber.id}--${number.id}"),
            subject = row,
            predicate = hasNumber,
            `object` = number
        ),
        cells = cellValues.mapIndexed { index, value ->
            val cell = createResource(ThingId("Cell_${row.id}_$index"))
            val column = createResource(ThingId("Column_$index"))
            CellGraph(
                rowHasCellStatement = createStatement(
                    id = StatementId("S_${row.id}--${hasCell.id}--${cell.id}"),
                    subject = row,
                    predicate = hasCell,
                    `object` = cell
                ),
                valueStatement = value?.let {
                    createStatement(
                        id = StatementId("S_${cell.id}--${hasValue.id}--${value.id}"),
                        subject = cell,
                        predicate = hasValue,
                        `object` = value
                    )
                },
                columnStatement = createStatement(
                    id = StatementId("S_${cell.id}--${hasValue.id}--${column.id}"),
                    subject = cell,
                    predicate = hasColumn,
                    `object` = column
                )
            )
        }
    )
}

fun createColumnGraph(id: ThingId): ColumnGraph {
    val column = createResource(id, classes = setOf(Classes.column))
    val table = createResource(ThingId("Table"), classes = setOf(Classes.table))
    val hasColumn = createPredicate(Predicates.csvwColumns)
    val hasTitle = createPredicate(Predicates.csvwTitles)
    val hasNumber = createPredicate(Predicates.csvwNumber)
    val title = createLiteral(ThingId("${id}_Title"), label = "Column $id Label")
    val number = createLiteral(ThingId("${id}_Number"), label = "1", datatype = Literals.XSD.INT.prefixedUri)
    return ColumnGraph(
        tableHasColumnStatement = createStatement(
            id = StatementId("S_${table.id}--${hasColumn.id}--${column.id}"),
            subject = table,
            predicate = hasColumn,
            `object` = column
        ),
        labelStatement = createStatement(
            id = StatementId("S_${column.id}--${hasTitle.id}--${title.id}"),
            subject = column,
            predicate = hasTitle,
            `object` = title
        ),
        indexStatement = createStatement(
            id = StatementId("S_${column.id}--${hasNumber.id}--${number.id}"),
            subject = column,
            predicate = hasNumber,
            `object` = number
        )
    )
}
