package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonPath.Type

data class SimpleComparisonPath(
    override val id: ThingId,
    override val type: Type,
    override val children: List<SimpleComparisonPath>,
) : ComparisonPath<SimpleComparisonPath>
