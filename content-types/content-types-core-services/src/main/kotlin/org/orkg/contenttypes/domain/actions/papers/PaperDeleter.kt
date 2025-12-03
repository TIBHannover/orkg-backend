package org.orkg.contenttypes.domain.actions.papers

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.DeletePaperCommand
import org.orkg.contenttypes.domain.actions.papers.DeletePaperAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class PaperDeleter(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val statementRepository: StatementRepository,
) : DeletePaperAction {
    override fun invoke(command: DeletePaperCommand, state: State): State {
        if (state.paper != null) {
            val statementsToDelete = mutableSetOf<StatementId>()
            val thingsToDelete = mutableSetOf<ThingId>()

            statementsToDelete += state.statements[command.paperId].orEmpty().map { it.id }
            thingsToDelete += command.paperId

            val authorListId = state.statements[command.paperId]
                ?.singleOrNull { it.predicate.id == Predicates.hasAuthors }
                ?.let { it.`object`.id }

            if (authorListId != null && statementRepository.countIncomingStatementsById(authorListId) <= 1L) {
                statementsToDelete += state.statements[authorListId].orEmpty().map { it.id }
                thingsToDelete += authorListId
            }

            val contributionIds = state.statements[command.paperId].orEmpty()
                .filter { it.predicate.id == Predicates.hasContribution }
                .map { it.`object`.id }

            thingsToDelete += contributionIds
            contributionIds.forEach { statementsToDelete += state.statements[it].orEmpty().map { it.id } }

            unsafeStatementUseCases.deleteAllById(statementsToDelete)
            thingsToDelete.forEach { id -> unsafeResourceUseCases.delete(id, command.contributorId) }
        }
        return state
    }
}
