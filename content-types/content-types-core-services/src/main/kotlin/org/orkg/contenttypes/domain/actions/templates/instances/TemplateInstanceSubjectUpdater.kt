package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction.State
import org.orkg.graph.output.ResourceRepository

class TemplateInstanceSubjectUpdater(
    private val resourceRepository: ResourceRepository,
) : UpdateTemplateInstanceAction {
    override fun invoke(command: UpdateTemplateInstanceCommand, state: State): State {
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
