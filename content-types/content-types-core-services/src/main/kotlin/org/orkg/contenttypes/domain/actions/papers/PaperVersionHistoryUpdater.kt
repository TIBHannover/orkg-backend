package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.PublishPaperCommand
import org.orkg.contenttypes.domain.actions.papers.PublishPaperAction.State
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

/**
 * Updates the publication history of a paper according to [documentation](https://gitlab.com/TIBHannover/orkg/orkg-frontend/-/wikis/Modeling-of-persistent-identification-of-ORKG-papers)
 */
class PaperVersionHistoryUpdater(
    private val statementService: StatementUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) : PublishPaperAction {
    override fun invoke(command: PublishPaperCommand, state: State): State {
        val hasPreviousVersionStatements = state.statements[command.id].orEmpty()
            .wherePredicate(Predicates.hasPreviousVersion)
        if (hasPreviousVersionStatements.isNotEmpty()) {
            statementService.deleteAllById(hasPreviousVersionStatements.map { it.id }.toSet())
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.paperVersionId!!,
                    predicateId = Predicates.hasPreviousVersion,
                    objectId = hasPreviousVersionStatements.first().`object`.id
                )
            )
        }
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = command.contributorId,
                subjectId = command.id,
                predicateId = Predicates.hasPreviousVersion,
                objectId = state.paperVersionId!!
            )
        )
        return state
    }
}
