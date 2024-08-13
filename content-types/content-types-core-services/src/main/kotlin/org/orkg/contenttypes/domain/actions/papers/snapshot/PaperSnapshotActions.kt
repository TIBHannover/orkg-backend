package org.orkg.contenttypes.domain.actions.papers.snapshot

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.input.PublishPaperUseCase
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Resource

interface SnapshotPaperAction : Action<PublishPaperUseCase.PublishCommand, SnapshotPaperAction.State> {
    data class State(
        val resource: Resource? = null,
        val paper: Paper? = null,
        val statements: Map<ThingId, List<GeneralStatement>> = emptyMap(),
        val paperVersionId: ThingId? = null
    )
}
