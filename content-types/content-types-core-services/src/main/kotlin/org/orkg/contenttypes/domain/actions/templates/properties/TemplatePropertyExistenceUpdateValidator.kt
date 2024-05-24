package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateService
import org.orkg.contenttypes.domain.actions.UpdateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.UpdateTemplatePropertyAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class TemplatePropertyExistenceUpdateValidator(
    private val templateService: TemplateService,
    private val resourceRepository: ResourceRepository
) : UpdateTemplatePropertyAction {
    override fun invoke(command: UpdateTemplatePropertyCommand, state: State): State {
        val resource = resourceRepository.findById(command.templateId)
            .filter { Classes.nodeShape in it.classes }
            .orElseThrow { TemplateNotFound(command.templateId) }
        val subgraph = templateService.findSubgraph(resource)
        val template = Template.from(resource, subgraph.statements)
        return state.copy(template = template, statements = subgraph.statements)
    }
}
