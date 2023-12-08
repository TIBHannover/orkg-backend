package org.orkg.contenttypes.domain.actions.template.property

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.TemplateClosed
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyAction.State
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.StatementRepository

class TemplatePropertyTemplateValidator(
    private val statementRepository: StatementRepository
) : TemplatePropertyAction {
    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State {
        val statements = statementRepository.findAllBySubject(command.templateId, PageRequests.ALL)
        statements.firstOrNull {
            it.predicate.id == Predicates.shClosed && it.`object` is Literal
                && (it.`object` as Literal).datatype == Literals.XSD.BOOLEAN.prefixedUri
                && it.`object`.label.equals("true", ignoreCase = true)
        }?.let { throw TemplateClosed(command.templateId) }
        val propertyCount = statements.count { it.predicate.id == Predicates.shProperty }
        return state.copy(propertyCount = propertyCount)
    }
}