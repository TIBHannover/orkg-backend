package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState

class LiteratureListModifiableValidator : UpdateLiteratureListAction {
    override fun invoke(
        command: UpdateLiteratureListCommand,
        state: UpdateLiteratureListState
    ): UpdateLiteratureListState = state.apply {
        if (literatureList!!.published) {
            throw LiteratureListNotModifiable(command.literatureListId)
        }
    }
}
