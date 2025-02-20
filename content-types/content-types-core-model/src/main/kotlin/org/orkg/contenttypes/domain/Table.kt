package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

data class Table(
    val id: ThingId,
    val label: String,
    val rows: List<Row>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null,
) {
    data class Row(
        val label: String?,
        val data: List<Thing?>,
    )

    companion object {
        fun from(resource: Resource, statements: Map<ThingId, List<GeneralStatement>>): Table {
            val directStatements = statements[resource.id].orEmpty()
            val header = directStatements.wherePredicate(Predicates.csvwColumns)
                .map { column ->
                    val columnStatements = statements[column.`object`.id].orEmpty()
                    val thing = columnStatements.wherePredicate(Predicates.csvwTitles).singleOrNull()?.`object`
                    val columnIndex =
                        columnStatements.wherePredicate(Predicates.csvwNumber).singleObjectLabel()?.toIntOrNull()
                    columnIndex to thing
                }
                .filter { it.first != null }
                .sortedBy { it.first }
            val headerIndices = header.map { it.first!! }
            val headerRow = Row(null, header.map { it.second })
            val data = directStatements.wherePredicate(Predicates.csvwRows)
                .map { row ->
                    val rowStatements = statements[row.`object`.id].orEmpty()
                    val rowIndex =
                        rowStatements.wherePredicate(Predicates.csvwNumber).singleObjectLabel()?.toIntOrNull()
                    val label = rowStatements.wherePredicate(Predicates.csvwTitles).singleObjectLabel()
                    val columnToCell = rowStatements.wherePredicate(Predicates.csvwCells)
                        .map { cell ->
                            val cellStatements = statements[cell.`object`.id].orEmpty()
                            val columnIndex = cellStatements.wherePredicate(Predicates.csvwColumn).singleOrNull()
                                ?.let { column ->
                                    statements[column.`object`.id].orEmpty()
                                        .wherePredicate(Predicates.csvwNumber).singleObjectLabel()?.toIntOrNull()
                                }
                            val thing = cellStatements.wherePredicate(Predicates.csvwValue).singleOrNull()?.`object`
                            columnIndex to thing
                        }
                        .filter { it.first in headerIndices }
                        .toMap()
                    rowIndex to Row(label, headerIndices.map { columnIndex -> columnToCell[columnIndex] })
                }
                .filter { it.first != null }
                .sortedBy { it.first }
                .map { it.second }
            return Table(
                id = resource.id,
                label = resource.label,
                rows = listOf(headerRow) + data,
                organizations = listOf(resource.organizationId),
                observatories = listOf(resource.observatoryId),
                extractionMethod = resource.extractionMethod,
                createdAt = resource.createdAt,
                createdBy = resource.createdBy,
                visibility = resource.visibility,
                unlistedBy = resource.unlistedBy
            )
        }
    }
}
