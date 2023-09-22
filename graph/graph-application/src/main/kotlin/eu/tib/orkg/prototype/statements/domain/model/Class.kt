package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

data class Class(
    override val id: ThingId,
    override val label: String,
    val uri: URI?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.createUnknownContributor(),
    val description: String? = null
) : Thing

fun Class?.toOptional() = Optional.ofNullable(this)
