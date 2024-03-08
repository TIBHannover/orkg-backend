package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateComparisonCommand
import org.orkg.contenttypes.domain.actions.comparisons.ComparisonAction.State

interface ComparisonAction : Action<CreateComparisonCommand, State> {
    data class State(
        val authors: List<Author> = emptyList(),
        val comparisonId: ThingId? = null
    )
}
