package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractTableColumnCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        tableId: ThingId,
        index: Int,
        titleLiteralId: ThingId,
    ): ThingId {
        val columnId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = "",
                classes = setOf(Classes.column),
            )
        )
        val columnNumberLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = "${index + 1}",
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = columnId,
                predicateId = Predicates.csvwNumber,
                objectId = columnNumberLiteralId
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = columnId,
                predicateId = Predicates.csvwTitles,
                objectId = titleLiteralId
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = tableId,
                predicateId = Predicates.csvwColumns,
                objectId = columnId
            )
        )
        return columnId
    }
}
