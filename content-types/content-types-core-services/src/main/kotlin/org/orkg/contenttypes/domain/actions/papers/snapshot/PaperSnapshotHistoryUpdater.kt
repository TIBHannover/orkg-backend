package org.orkg.contenttypes.domain.actions.papers.snapshot

import org.orkg.contenttypes.domain.actions.papers.snapshot.SnapshotPaperAction.State
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

/**
 * Updates the publication history of a paper according to [documentation](https://gitlab.com/TIBHannover/orkg/orkg-frontend/-/wikis/Modeling-of-persistent-identification-of-ORKG-papers)
 */
class PaperSnapshotHistoryUpdater(
    private val statementService: StatementUseCases
) : SnapshotPaperAction {
    override fun invoke(command: PublishPaperUseCase.PublishCommand, state: State): State {
        val hasPreviousVersionStatements = state.statements[command.id].orEmpty()
            .wherePredicate(Predicates.hasPreviousVersion)
        if (hasPreviousVersionStatements.isNotEmpty()) {
            statementService.delete(hasPreviousVersionStatements.map { it.id }.toSet())
            statementService.add(
                userId = command.contributorId,
                subject = state.paperVersionId!!,
                predicate = Predicates.hasPreviousVersion,
                `object` = hasPreviousVersionStatements.first().`object`.id
            )
        }
        statementService.add(
            userId = command.contributorId,
            subject = command.id,
            predicate = Predicates.hasPreviousVersion,
            `object` = state.paperVersionId!!
        )
        return state
    }
}
