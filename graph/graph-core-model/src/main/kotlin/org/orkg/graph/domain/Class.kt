package org.orkg.graph.domain

import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId

/** The set of classes that can be published, meaning a DOI can be registered for them. */
val PUBLISHABLE_CLASSES: Set<ThingId> = setOf(
    ThingId("Paper"),
    ThingId("Comparison"),
    ThingId("SmartReviewPublished"),
)

data class Class(
    override val id: ThingId,
    override val label: String,
    val uri: URI?,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val description: String? = null
) : Thing

fun Class?.toOptional() = Optional.ofNullable(this)
