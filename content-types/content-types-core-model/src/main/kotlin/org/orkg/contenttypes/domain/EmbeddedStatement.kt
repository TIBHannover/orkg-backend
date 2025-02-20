package org.orkg.contenttypes.domain

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.domain.Thing
import java.time.OffsetDateTime

data class EmbeddedStatement(
    val thing: Thing,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val statements: Map<ThingId, List<EmbeddedStatement>>,
)
