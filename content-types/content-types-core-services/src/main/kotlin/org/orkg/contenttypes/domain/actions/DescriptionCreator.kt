package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

abstract class DescriptionCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) {
    internal fun create(contributorId: ContributorId, subjectId: ThingId, description: String) {
        val literal = literalService.create(
            userId = contributorId,
            label = description
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.description,
            `object` = literal.id
        )
    }
}
