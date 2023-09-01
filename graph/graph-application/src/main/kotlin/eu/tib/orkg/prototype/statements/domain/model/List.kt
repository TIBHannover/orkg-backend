package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class List(
    val id: ThingId,
    val label: String,
    val elements: kotlin.collections.List<ThingId>,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor()
)
