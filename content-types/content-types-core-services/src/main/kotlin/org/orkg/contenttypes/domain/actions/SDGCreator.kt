package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

class SDGCreator(
    private val statementService: StatementUseCases
) {
    internal fun create(contributorId: ContributorId, sdgs: Set<ThingId>, subjectId: ThingId) {
        sdgs.forEach { id ->
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sustainableDevelopmentGoal,
                `object` = id
            )
        }
    }
}
