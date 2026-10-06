@file:Suppress("ktlint:standard:filename")

package org.orkg.contenttypes.domain.actions.comparisons.resources

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand

interface CreateComparisonRelatedResourceAction : Action<CreateComparisonRelatedResourceCommand, CreateComparisonRelatedResourceAction.State> {
    data class State(
        val comparisonRelatedResourceId: ThingId? = null,
    )
}
