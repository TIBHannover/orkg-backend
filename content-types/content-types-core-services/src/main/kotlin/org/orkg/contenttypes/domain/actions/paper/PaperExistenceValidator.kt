package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.contenttypes.input.PaperUseCases

class PaperExistenceValidator(
    private val paperService: PaperUseCases
) : UpdatePaperAction {
    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        val paper = paperService.findById(command.paperId)
            .orElseThrow { PaperNotFound(command.paperId) }
        return state.copy(paper = paper)
    }
}
