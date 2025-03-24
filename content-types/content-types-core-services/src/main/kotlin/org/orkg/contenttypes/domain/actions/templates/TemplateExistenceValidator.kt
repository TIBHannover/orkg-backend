package org.orkg.contenttypes.domain.actions.templates

import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateService
import org.orkg.contenttypes.domain.actions.UpdateTemplateCommand
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class TemplateExistenceValidator(
    private val templateService: TemplateService,
    private val resourceRepository: ResourceRepository,
) : UpdateTemplateAction {
    override fun invoke(command: UpdateTemplateCommand, state: State): State {
        val resource = resourceRepository.findById(command.templateId)
            .filter { Classes.nodeShape in it.classes }
            .orElseThrow { TemplateNotFound(command.templateId) }
        val subgraph = templateService.findSubgraph(resource)
        val template = Template.from(resource, subgraph.statements)
        return state.copy(template = template, statements = subgraph.statements)
    }
}
