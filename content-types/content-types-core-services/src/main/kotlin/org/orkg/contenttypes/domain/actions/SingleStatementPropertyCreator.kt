package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class SingleStatementPropertyCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) {
    internal fun create(contributorId: ContributorId, subjectId: ThingId, predicateId: ThingId, label: String) {
        val literal = literalService.create(
            CreateCommand(
                contributorId = contributorId,
                label = label
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = predicateId,
            `object` = literal
        )
    }
}
