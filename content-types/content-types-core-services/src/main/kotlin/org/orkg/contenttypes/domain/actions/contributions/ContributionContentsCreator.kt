package org.orkg.contenttypes.domain.actions.contributions

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class ContributionContentsCreator(
    private val contributionCreator: ContributionCreator,
) : ContributionAction {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        ContributionCreator(
            classService,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState =
        state.copy(
            contributionId = contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = command,
                contributionDefinitions = listOf(command.contribution),
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            ).single()
        )
}
