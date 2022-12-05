package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

data class Class(
    val id: ClassId?,
    val label: String,
    val uri: URI?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    // This is added to replace @JsonTypeInfo on the Thing interface
    val _class: String? = "class"
) : Thing {
    var description: String? = null
    override val thingId: ThingId = id!!
    fun toClass(): Class = Class(id, label, uri, createdAt, createdBy, _class)
}

fun Class?.toOptional() = Optional.ofNullable(this)
