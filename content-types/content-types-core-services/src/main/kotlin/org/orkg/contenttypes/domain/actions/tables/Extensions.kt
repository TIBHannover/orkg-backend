package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Table
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates

internal fun Table.Row.toCreateRowCommand(): CreateRowCommand =
    CreateRowCommand(
        label = label,
        data = data.map { it?.id?.value }
    )

internal fun parseRowGraphs(rootId: ThingId, statements: Map<ThingId, List<GeneralStatement>>, headerIndices: List<Int>): List<RowGraph> =
    statements[rootId].orEmpty<GeneralStatement>().wherePredicate(Predicates.csvwRows)
        .map { row ->
            val rowStatements = statements[row.`object`.id].orEmpty()
            val rowIndexStatement = rowStatements.wherePredicate(Predicates.csvwNumber).singleOrNull()
            val rowIndex = rowIndexStatement?.`object`?.label?.toIntOrNull()
            val labelStatement = rowStatements.wherePredicate(Predicates.csvwTitles).singleOrNull()
            val columnToCell = rowStatements.wherePredicate(Predicates.csvwCells)
                .map { cell ->
                    val cellStatements = statements[cell.`object`.id].orEmpty()
                    val columnStatement = cellStatements.wherePredicate(Predicates.csvwColumn).singleOrNull()
                    val columnIndex = columnStatement?.let { column ->
                        statements[column.`object`.id].orEmpty()
                            .wherePredicate(Predicates.csvwNumber).singleOrNull()?.`object`?.label?.toIntOrNull()
                    }
                    val valueStatement = cellStatements.wherePredicate(Predicates.csvwValue).singleOrNull()
                    columnIndex to CellGraph(cell, valueStatement, columnStatement)
                }
                .filter { it.first in headerIndices }
                .toMap()
            val cells = headerIndices.map { columnIndex -> columnToCell[columnIndex] }
            rowIndex to RowGraph(row, labelStatement, rowIndexStatement, cells)
        }
        .filter { it.first != null }
        .sortedBy { it.first }
        .map { it.second }

internal fun parseColumnGraphs(rootId: ThingId, statements: Map<ThingId, List<GeneralStatement>>): List<Pair<Int?, ColumnGraph>> =
    statements[rootId].orEmpty()
        .wherePredicate(Predicates.csvwColumns)
        .map { column ->
            val columnStatements = statements[column.`object`.id].orEmpty()
            val columnIndexStatement = columnStatements.wherePredicate(Predicates.csvwNumber).singleOrNull()
            val columnIndex = columnIndexStatement?.`object`?.label?.toIntOrNull()
            val labelStatement = columnStatements.wherePredicate(Predicates.csvwTitles).singleOrNull()
            columnIndex to ColumnGraph(column, labelStatement, columnIndexStatement)
        }
        .filter { it.first != null }
        .sortedBy { it.first }
