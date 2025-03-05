package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class PaperContributionCreator(
    private val contributionCreator: ContributionCreator,
) : CreatePaperAction {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        ContributionCreator(
            classService,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            literalService,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    override operator fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        command.contents?.let {
            contributionCreator.create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = it,
                contributionDefinitions = it.contributions,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        }
        return state
    }
}
