package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator
) : CreateTemplateAction {
    constructor(
        literalService: LiteralUseCases,
        statementService: StatementUseCases
    ) : this(SingleStatementPropertyCreator(literalService, statementService))

    override fun invoke(command: CreateTemplateCommand, state: State): State =
        state.apply {
            command.description?.let {
                singleStatementPropertyCreator.create(
                    contributorId = command.contributorId,
                    subjectId = state.templateId!!,
                    predicateId = Predicates.description,
                    label = it
                )
            }
        }
}
