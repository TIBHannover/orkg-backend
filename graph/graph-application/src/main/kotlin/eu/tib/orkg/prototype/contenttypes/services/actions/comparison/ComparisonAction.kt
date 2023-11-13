package eu.tib.orkg.prototype.contenttypes.services.actions.comparison

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.services.actions.Action
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateComparisonCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction.State
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ComparisonAction : Action<CreateComparisonCommand, State> {
    data class State(
        val authors: List<Author> = emptyList(),
        val comparisonId: ThingId? = null
    )
}
