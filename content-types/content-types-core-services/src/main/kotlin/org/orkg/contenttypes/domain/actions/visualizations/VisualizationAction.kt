package org.orkg.contenttypes.domain.actions.visualizations

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction.State

interface VisualizationAction : Action<CreateVisualizationCommand, State> {
    data class State(
        val authors: List<Author> = emptyList(),
        val visualizationId: ThingId? = null,
    )
}
