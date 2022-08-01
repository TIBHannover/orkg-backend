package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.time.OffsetDateTime

data class Literal(
    val id: LiteralId?,
    val label: String,
    val datatype: String = "xsd:string",
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "literal"
) : Thing {
    override val thingId: ThingId
        get() = ThingId(id!!.value)
}
