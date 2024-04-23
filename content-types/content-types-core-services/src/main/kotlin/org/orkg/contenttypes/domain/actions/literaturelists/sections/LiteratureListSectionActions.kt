package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand

interface CreateLiteratureListSectionAction : Action<CreateLiteratureListSectionCommand, CreateLiteratureListSectionAction.State> {
    data class State(
        val literatureListSectionId: ThingId? = null
    )
}
