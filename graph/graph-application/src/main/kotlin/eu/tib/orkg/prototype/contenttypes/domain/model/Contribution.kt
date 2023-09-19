package eu.tib.orkg.prototype.contenttypes.domain.model

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility

typealias ContributionSubGraph = Map<ThingId, List<ThingId>>

data class Contribution(
    val id: ThingId,
    val label: String,
    val classes: Set<ThingId>,
    val properties: ContributionSubGraph,
    val visibility: Visibility
)

internal fun Iterable<GeneralStatement>.toContributionSubGraph(): ContributionSubGraph = this
    .groupBy { it.predicate.id }
    .mapValues {
        it.value.map { statement -> statement.`object`.id }
    }
