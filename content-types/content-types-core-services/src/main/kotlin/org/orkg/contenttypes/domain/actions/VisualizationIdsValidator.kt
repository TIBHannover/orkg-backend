package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class VisualizationIdsValidator<T, S>(
    resourceRepository: ResourceRepository,
    valueSelector: (T) -> List<ThingId>?,
) : ResourceValidator<T, S>(
        resourceRepository = resourceRepository,
        newValueSelector = { valueSelector(it)?.toSet() },
        includeClasses = setOf(Classes.visualization),
        exceptionFactory = ::VisualizationNotFound
    )
