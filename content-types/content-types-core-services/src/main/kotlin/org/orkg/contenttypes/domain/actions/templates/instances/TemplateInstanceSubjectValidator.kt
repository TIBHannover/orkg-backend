package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.TemplateInstanceService
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.output.ResourceRepository

class TemplateInstanceSubjectValidator(
    private val resourceRepository: ResourceRepository,
    private val templateInstanceService: TemplateInstanceService,
) : UpdateTemplateInstanceAction {
    override fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState {
        val subject = resourceRepository.findById(command.subject)
            .orElseThrow { ResourceNotFound.withId(command.subject) }
        return state.copy(
            templateInstance = templateInstanceService.run { subject.toTemplateInstance(state.template!!) }
        )
    }
}
