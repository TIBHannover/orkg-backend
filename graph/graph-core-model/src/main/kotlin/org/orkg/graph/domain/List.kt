package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.ThingId
import java.time.OffsetDateTime

data class List(
    val id: ThingId,
    val label: String,
    val elements: kotlin.collections.List<ThingId>,
    val createdAt: OffsetDateTime,
    val uri: IRI? = null,
    val createdBy: ContributorId = ContributorId.UNKNOWN,
    val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    val modifiable: Boolean = true,
)
