package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.CreateTemplateInstanceAction.State
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases

class TemplateInstanceSubjectCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : CreateTemplateInstanceAction {
    override fun invoke(command: CreateTemplateInstanceCommand, state: State): State {
        val templateInstanceId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.label,
                classes = command.additionalClasses + state.template!!.targetClass.id,
                extractionMethod = command.extractionMethod,
            ),
        )
        return state.copy(templateInstanceId = templateInstanceId)
    }
}
