package org.orkg.community.domain

import java.time.LocalDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.PredicatePath

data class ObservatoryFilter(
    val id: ObservatoryFilterId,
    val observatoryId: ObservatoryId,
    val label: String,
    val createdBy: ContributorId,
    val createdAt: LocalDateTime,
    val path: PredicatePath,
    val range: ThingId,
    val exact: Boolean,
    val featured: Boolean = false
)
