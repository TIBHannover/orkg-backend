package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.graph.output.ResourceRepository

class TemplateInstanceSubjectUpdater(
    private val resourceRepository: ResourceRepository,
) : UpdateTemplateInstanceAction {
    override operator fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState {
        val subject = state.templateInstance!!.root
        if (state.template!!.targetClass.id !in subject.classes) {
            val updated = subject.copy(classes = subject.classes + state.template.targetClass.id)
            resourceRepository.save(updated)
            return state.copy(
                templateInstance = state.templateInstance.copy(
                    root = updated
                )
            )
        }
        return state
    }
}
