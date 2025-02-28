package org.orkg.graph.domain

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import java.time.OffsetDateTime
import java.util.Optional

/** The set of classes that can be published, meaning a DOI can be registered for them. */
val PUBLISHABLE_CLASSES: Set<ThingId> = setOf(
    Classes.paper,
    Classes.comparison,
    Classes.smartReviewPublished,
)

data class Class(
    override val id: ThingId,
    override val label: String,
    val uri: ParsedIRI?,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    override val modifiable: Boolean = true,
) : Thing

fun Class?.toOptional() = Optional.ofNullable(this)
