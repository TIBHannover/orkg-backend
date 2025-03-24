package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateRelationsUpdater(
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater,
) : UpdateTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        StatementCollectionPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases),
        SingleStatementPropertyUpdater(unsafeLiteralUseCases, statementService, unsafeStatementUseCases)
    )

    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        command.relations?.also { relations ->
            val statements = state.statements[command.templateId].orEmpty()
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objects = relations.researchFields
            )
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objects = relations.researchProblems
            )
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectId = relations.predicate
            )
        }
        return state
    }
}
