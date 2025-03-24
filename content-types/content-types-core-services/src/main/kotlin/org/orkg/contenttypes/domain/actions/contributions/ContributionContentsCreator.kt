package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class ContributionContentsCreator(
    private val contributionCreator: ContributionCreator,
) : ContributionAction {
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

    override fun invoke(command: CreateContributionCommand, state: State): State =
        state.copy(
            contributionId = contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command,
                contributionCommands = listOf(command.contribution),
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            ).single()
        )
}
