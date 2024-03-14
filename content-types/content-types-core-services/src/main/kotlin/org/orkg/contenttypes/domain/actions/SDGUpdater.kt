package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class SDGUpdater(
    private val statementService: StatementUseCases
) {
    internal fun update(contributorId: ContributorId, sdgs: Set<ThingId>, subjectId: ThingId) {
        val statements = statementService.findAll(
            subjectId = subjectId,
            predicateId = Predicates.sustainableDevelopmentGoal,
            pageable = PageRequests.ALL
        )
        val statementsToRemove = statements.content.filter { it.`object`.id !in sdgs }.map { it.id }
        if (statementsToRemove.isNotEmpty()) {
            statementService.delete(statementsToRemove.toSet())
        }
        sdgs.forEach { id ->
            if (statements.none { it.`object`.id == id }) {
                statementService.add(
                    userId = contributorId,
                    subject = subjectId,
                    predicate = Predicates.sustainableDevelopmentGoal,
                    `object` = id
                )
            }
        }
    }
}
