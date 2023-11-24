package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Visibility

typealias ContributionSubGraph = Map<ThingId, List<ThingId>>

data class Contribution(
    val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val properties: ContributionSubGraph,
    val visibility: Visibility
)

fun Iterable<GeneralStatement>.toContributionSubGraph(): ContributionSubGraph = this
    .groupBy { it.predicate.id }
    .mapValues {
        it.value.map { statement -> statement.`object`.id }
    }
