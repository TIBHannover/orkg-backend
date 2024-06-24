package org.orkg.contenttypes.domain.actions.literaturelists.sections

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateLiteratureListSectionCommand
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionCommand
import org.orkg.graph.domain.GeneralStatement

interface CreateLiteratureListSectionAction : Action<CreateLiteratureListSectionCommand, CreateLiteratureListSectionAction.State> {
    data class State(
        val literatureListSectionId: ThingId? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}

interface UpdateLiteratureListSectionAction : Action<UpdateLiteratureListSectionCommand, UpdateLiteratureListSectionAction.State> {
    data class State(
        val literatureList: LiteratureList? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}
