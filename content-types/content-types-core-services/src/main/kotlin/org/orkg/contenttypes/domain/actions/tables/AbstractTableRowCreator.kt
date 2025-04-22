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

class AbstractTableRowCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        tableId: ThingId,
        index: Int,
        label: String?,
    ): ThingId {
        val rowId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = "",
                classes = setOf(Classes.row),
            )
        )
        val rowNumberLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = "${index + 1}",
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = rowId,
                predicateId = Predicates.csvwNumber,
                objectId = rowNumberLiteralId
            )
        )
        if (label != null) {
            val rowLabelLiteralId = unsafeLiteralUseCases.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = label
                )
            )
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = rowId,
                    predicateId = Predicates.csvwTitles,
                    objectId = rowLabelLiteralId
                )
            )
        }
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = tableId,
                predicateId = Predicates.csvwRows,
                objectId = rowId
            )
        )
        return rowId
    }
}
