package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId

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
