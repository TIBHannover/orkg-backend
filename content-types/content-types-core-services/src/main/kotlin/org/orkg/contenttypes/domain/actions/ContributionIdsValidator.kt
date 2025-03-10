package org.orkg.contenttypes.domain.actions

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository

class ContributionIdsValidator<T, S>(
    resourceRepository: ResourceRepository,
    valueSelector: (T) -> List<ThingId>?,
) : ResourceValidator<T, S>(
        resourceRepository = resourceRepository,
        newValueSelector = { valueSelector(it)?.toSet() },
        includeClasses = setOf(Classes.contribution),
        exceptionFactory = ::ContributionNotFound
    )
