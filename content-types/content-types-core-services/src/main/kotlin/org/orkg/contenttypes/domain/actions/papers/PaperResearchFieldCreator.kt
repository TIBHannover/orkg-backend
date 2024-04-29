package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyCreator
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class PaperResearchFieldCreator(
    private val statementCollectionPropertyCreator: StatementCollectionPropertyCreator
) : CreatePaperAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(StatementCollectionPropertyCreator(literalService, statementService))

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.apply {
            statementCollectionPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.paperId!!,
                predicateId = Predicates.hasResearchField,
                objects = command.researchFields
            )
        }
}
