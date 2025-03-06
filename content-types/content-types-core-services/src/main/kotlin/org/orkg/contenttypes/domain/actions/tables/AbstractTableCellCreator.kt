package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractTableCellCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        rowId: ThingId,
        columnId: ThingId,
        value: ThingId?,
    ): ThingId {
        val cellId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = "",
                classes = setOf(Classes.cell),
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = cellId,
                predicateId = Predicates.csvwColumn,
                objectId = columnId
            )
        )
        if (value != null) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = cellId,
                    predicateId = Predicates.csvwValue,
                    objectId = value
                )
            )
        }
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = rowId,
                predicateId = Predicates.csvwCells,
                objectId = cellId
            )
        )
        return cellId
    }
}
