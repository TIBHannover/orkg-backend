package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.ThingId
import java.time.OffsetDateTime
import java.util.Optional

data class Class(
    override val id: ThingId,
    override val label: String,
    val uri: IRI?,
    override val createdAt: OffsetDateTime,
    override val createdBy: ContributorId = ContributorId.UNKNOWN,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    override val modifiable: Boolean = true,
) : Thing

fun Class?.toOptional() = Optional.ofNullable(this)
