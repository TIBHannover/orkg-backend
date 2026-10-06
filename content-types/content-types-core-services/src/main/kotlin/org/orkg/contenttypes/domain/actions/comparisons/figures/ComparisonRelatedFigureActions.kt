@file:Suppress("ktlint:standard:filename")

package org.orkg.contenttypes.domain.actions.comparisons.figures

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand

interface CreateComparisonRelatedFigureAction : Action<CreateComparisonRelatedFigureCommand, CreateComparisonRelatedFigureAction.State> {
    data class State(
        val comparisonRelatedFigureId: ThingId? = null,
    )
}
