package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class PaperContributionCreator(
    private val contributionCreator: ContributionCreator,
) : CreatePaperAction {
    constructor(
        unsafeClassUseCases: UnsafeClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        ContributionCreator(
            unsafeClassUseCases,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    override fun invoke(command: CreatePaperCommand, state: State): State {
        command.contents?.also {
            contributionCreator.create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = it,
                contributionCommands = it.contributions,
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            )
        }
        return state
    }
}
