package org.orkg.contenttypes.domain.actions.papers

import dev.forkhandles.values.ofOrNull
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.Label
import org.orkg.graph.input.ResourceUseCases

class PaperTitleUpdateValidator(
    private val resourceService: ResourceUseCases,
) : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.title != null && command.title != state.paper!!.title) {
            Label.ofOrNull(command.title!!) ?: throw InvalidLabel("title")
            if (resourceService.findAllPapersByTitle(command.title).any { it.id != command.paperId }) {
                throw PaperAlreadyExists.withTitle(command.title!!)
            }
        }
        return state
    }
}
