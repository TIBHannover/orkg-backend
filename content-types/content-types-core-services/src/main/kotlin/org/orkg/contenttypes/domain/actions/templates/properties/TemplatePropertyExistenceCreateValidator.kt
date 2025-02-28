package org.orkg.contenttypes.domain.actions.templates.properties

import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.actions.CreateTemplatePropertyCommand
import org.orkg.contenttypes.domain.actions.templates.properties.CreateTemplatePropertyAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class TemplatePropertyExistenceCreateValidator(
    private val resourceRepository: ResourceRepository,
) : CreateTemplatePropertyAction {
    override fun invoke(command: CreateTemplatePropertyCommand, state: State): State {
        resourceRepository.findById(command.templateId)
            .filter { Classes.nodeShape in it.classes }
            .orElseThrow { TemplateNotFound(command.templateId) }
        return state
    }
}
