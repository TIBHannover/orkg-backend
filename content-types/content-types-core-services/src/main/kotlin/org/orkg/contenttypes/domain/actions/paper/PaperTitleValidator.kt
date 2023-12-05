package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.input.ResourceUseCases

class PaperTitleValidator(
    private val resourceService: ResourceUseCases
) : CreatePaperAction {
    override fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        val resource = resourceService.findAllByTitle(command.title).firstOrNull()
        if (resource != null) {
            throw PaperAlreadyExists.withTitle(resource.label)
        }
        return state
    }
}
