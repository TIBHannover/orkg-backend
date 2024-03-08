package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.actions.CreateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.TemplateAction.State
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases

class TemplateClosedCreator(
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) : TemplateAction {
    override fun invoke(command: CreateTemplateCommand, state: State): State {
        if (command.isClosed) {
            statementService.add(
                userId = command.contributorId,
                subject = state.templateId!!,
                predicate = Predicates.shClosed,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = command.contributorId,
                        label = "true",
                        datatype = Literals.XSD.BOOLEAN.prefixedUri
                    )
                )
            )
        }
        return state
    }
}
