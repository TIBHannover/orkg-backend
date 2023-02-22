package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class Literal(
    val id: ThingId,
    override val label: String,
    val datatype: String = "xsd:string",
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
) : Thing {
    override val thingId: ThingId = ThingId(id.value)
}
