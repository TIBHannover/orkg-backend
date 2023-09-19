package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.time.OffsetDateTime

data class Predicate(
    override val id: ThingId,
    override val label: String,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    val description: String? = null
) : Thing
