package org.orkg.contenttypes.domain.actions.papers

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.DeletePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing

interface CreatePaperAction : Action<CreatePaperCommand, CreatePaperAction.State> {
    data class State(
        val validationCache: Map<String, Either<CreateThingCommandPart, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val authors: List<Author> = emptyList(),
        val paperId: ThingId? = null,
    )
}

interface UpdatePaperAction : Action<UpdatePaperCommand, UpdatePaperAction.State> {
    data class State(
        val paper: Paper? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val authors: List<Author> = emptyList(),
    )
}

interface DeletePaperAction : Action<DeletePaperCommand, DeletePaperAction.State> {
    data class State(
        val paper: Resource? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
    )
}

interface PublishPaperAction : Action<PublishPaperUseCase.PublishCommand, PublishPaperAction.State> {
    data class State(
        val paper: Paper? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val paperVersionId: ThingId? = null,
    )
}
