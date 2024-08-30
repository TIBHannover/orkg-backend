package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.actions.PublishLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.PublishLiteratureListAction.State
import org.orkg.contenttypes.input.LiteratureListUseCases

class LiteratureListPublishableValidator(
    private val literatureListService: LiteratureListUseCases
) : PublishLiteratureListAction {
    override fun invoke(command: PublishLiteratureListCommand, state: State): State {
        val literatureList = literatureListService.findById(command.id)
            .orElseThrow { LiteratureListNotFound(command.id) }
        if (literatureList.published) {
            throw LiteratureListAlreadyPublished(command.id)
        }
        return state.copy(literatureList = literatureList)
    }
}
