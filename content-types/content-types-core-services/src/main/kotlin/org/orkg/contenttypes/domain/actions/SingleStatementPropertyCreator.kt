package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SingleStatementPropertyCreator(
    private val literalService: LiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        subjectId: ThingId,
        predicateId: ThingId,
        label: String,
        datatype: String = Literals.XSD.STRING.prefixedUri
    ) {
        val literal = literalService.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = label,
                datatype = datatype
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = predicateId,
                objectId = literal
            )
        )
    }
}
