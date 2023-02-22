package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class Predicate(
    val id: ThingId,
    override val label: String,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
) : Thing {
    var description: String? = null
    override val thingId: ThingId = ThingId(id.value)
}
