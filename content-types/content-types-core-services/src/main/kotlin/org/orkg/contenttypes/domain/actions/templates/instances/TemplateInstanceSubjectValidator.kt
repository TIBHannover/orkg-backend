package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.TemplateInstanceService
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction.State
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

class TemplateInstanceSubjectValidator(
    private val resourceRepository: ResourceRepository,
    private val templateInstanceService: TemplateInstanceService,
) : UpdateTemplateInstanceAction {
    override fun invoke(command: UpdateTemplateInstanceCommand, state: State): State {
        val subject = resourceRepository.findById(command.subject)
            .orElseThrow { ResourceNotFound(command.subject) }
        return state.copy(
            templateInstance = with(templateInstanceService) { subject.toTemplateInstance(state.template!!) }
        )
    }
}
