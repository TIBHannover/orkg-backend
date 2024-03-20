package org.orkg.contenttypes.domain.actions.papers

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.input.ResourceUseCases

class PaperTitleCreateValidator(
    private val resourceService: ResourceUseCases
) : CreatePaperAction {
    override fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState {
        Label.ofOrNull(command.title) ?: throw InvalidLabel("title")
        val resource = resourceService.findAllPapersByTitle(command.title).firstOrNull()
        if (resource != null) {
            throw PaperAlreadyExists.withTitle(resource.label)
        }
        return state
    }
}
