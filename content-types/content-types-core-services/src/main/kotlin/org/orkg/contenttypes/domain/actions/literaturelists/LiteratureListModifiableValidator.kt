package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction.State

class LiteratureListModifiableValidator : UpdateLiteratureListAction {
    override fun invoke(command: UpdateLiteratureListCommand, state: State): State {
        if (state.literatureList!!.published) {
            throw LiteratureListNotModifiable(command.literatureListId)
        }
        return state
    }
}
