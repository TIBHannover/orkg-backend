package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Visibility
import java.time.OffsetDateTime

typealias ContributionSubGraph = Map<ThingId, List<ThingId>>

data class Contribution(
    val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val properties: ContributionSubGraph,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val visibility: Visibility,
    val unlistedBy: ContributorId? = null,
)

fun Iterable<GeneralStatement>.toContributionSubGraph(): ContributionSubGraph = this
    .groupBy { it.predicate.id }
    .mapValues {
        it.value.map { statement -> statement.`object`.id }
    }
