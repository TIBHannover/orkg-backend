package org.orkg.contenttypes.domain.actions.paper

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.paper.PaperAction.State
import org.orkg.graph.domain.Thing

interface PaperAction : Action<CreatePaperCommand, State> {
    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val authors: List<Author> = emptyList(),
        val paperId: ThingId? = null
    )
}
