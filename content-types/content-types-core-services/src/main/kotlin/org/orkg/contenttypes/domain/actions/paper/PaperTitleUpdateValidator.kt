package org.orkg.contenttypes.domain.actions.paper

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.input.ResourceUseCases

class PaperTitleUpdateValidator(
    private val resourceService: ResourceUseCases
) : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        command.title?.let { title ->
            Label.ofOrNull(title) ?: throw InvalidLabel()
            if (resourceService.findAllPapersByTitle(title).any { it.id != command.paperId }) {
                throw PaperAlreadyExists.withTitle(title)
            }
        }
        return state
    }
}
