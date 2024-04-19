package org.orkg.contenttypes.domain.actions.literaturelists

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListCommand
import org.orkg.graph.domain.GeneralStatement

interface UpdateLiteratureListAction : Action<UpdateLiteratureListCommand, UpdateLiteratureListAction.State> {
    data class State(
        val literatureList: LiteratureList? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val authors: List<Author> = emptyList()
    )
}
