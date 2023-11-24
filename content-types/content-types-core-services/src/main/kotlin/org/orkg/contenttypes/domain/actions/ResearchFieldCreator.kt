package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

abstract class ResearchFieldCreator(
    private val statementService: StatementUseCases,
) {
    internal fun create(contributorId: ContributorId, researchFields: List<ThingId>, subjectId: ThingId) {
        researchFields.distinct().forEach {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasResearchField,
                `object` = it
            )
        }
    }
}
