package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction.State
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class TemplateDescriptionCreator(
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator,
) : CreateTemplateAction {
    constructor(
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
    ) : this(
        SingleStatementPropertyCreator(unsafeLiteralUseCases, unsafeStatementUseCases)
    )

    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.description?.also {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = state.templateId!!,
                predicateId = Predicates.description,
                label = it
            )
        }
        return state
    }
}
