package org.orkg.contenttypes.domain.actions.template

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.template.TemplateAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateMetadataCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        command.description?.let { description ->
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.description,
                `object` = literalService.create(
                    userId = command.contributorId,
                    label = description
                ).id
            )
        }
        if (command.isClosed) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shClosed,
                `object` = literalService.create(
                    userId = command.contributorId,
                    label = "true",
                    datatype = Literals.XSD.BOOLEAN.prefixedUri
                ).id
            )
        }
        return state
    }
}
