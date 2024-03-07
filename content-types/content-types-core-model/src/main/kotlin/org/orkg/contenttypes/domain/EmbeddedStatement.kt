package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing

data class EmbeddedStatement(
    val thing: Thing,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val statements: Map<ThingId, List<EmbeddedStatement>>
)
