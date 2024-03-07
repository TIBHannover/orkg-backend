package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.graph.domain.Resources

class SDGValidator<T, S>(
    private val newValueSelector: (T) -> Set<ThingId>?,
    private val oldValueSelector: (S) -> Set<ThingId> = { emptySet() }
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newSDGs = newValueSelector(command)
        val oldSDGs = oldValueSelector(state)
        if (newSDGs != null && newSDGs != oldSDGs) {
            (newSDGs - oldSDGs).forEach { id ->
                if (id !in Resources.sustainableDevelopmentGoals) {
                    throw SustainableDevelopmentGoalNotFound(id)
                }
            }
        }
        return state
    }
}
