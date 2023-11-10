package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId

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
