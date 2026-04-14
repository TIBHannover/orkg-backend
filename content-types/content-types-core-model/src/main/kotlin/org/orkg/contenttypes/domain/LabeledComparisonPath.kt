package org.orkg.contenttypes.domain

import org.orkg.common.ThingId

data class LabeledComparisonPath(
    override val id: ThingId,
    val label: String,
    val description: String?,
    override val type: ComparisonPath.Type,
    override val children: List<LabeledComparisonPath>,
    val sources: Int? = null,
) : ComparisonPath<LabeledComparisonPath>
