package org.orkg.contenttypes.domain.actions.smartreviews.sections

import org.orkg.contenttypes.domain.actions.CreateSmartReviewSectionCommand
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.smartreviews.AbstractSmartReviewSectionCreator
import org.orkg.contenttypes.domain.actions.smartreviews.sections.CreateSmartReviewSectionAction.State
import org.orkg.contenttypes.input.AbstractSmartReviewSectionCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class SmartReviewSectionCreator(
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val abstractSmartReviewSectionCreator: AbstractSmartReviewSectionCreator,
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
) : CreateSmartReviewSectionAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        unsafeStatementUseCases,
        AbstractSmartReviewSectionCreator(unsafeStatementUseCases, unsafeResourceUseCases, unsafeLiteralUseCases),
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: CreateSmartReviewSectionCommand, state: State): State {
        val sectionId = abstractSmartReviewSectionCreator.create(
            contributorId = command.contributorId,
            section = command as AbstractSmartReviewSectionCommand
        )
        if (command.index != null && command.index!! >= 0) {
            val sectionStatements = state.statements[state.contributionId!!].orEmpty()
            statementCollectionPropertyUpdater.update(
                statements = sectionStatements,
                contributorId = command.contributorId,
                subjectId = state.contributionId,
                predicateId = Predicates.hasSection,
                objects = sectionStatements.mapTo(mutableListOf()) { it.`object`.id }
                    .also { it.add(command.index!!.coerceAtMost(it.size), sectionId) }
            )
        } else {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = state.contributionId!!,
                    predicateId = Predicates.hasSection,
                    objectId = sectionId
                )
            )
        }
        return state.copy(smartReviewSectionId = sectionId)
    }
}
