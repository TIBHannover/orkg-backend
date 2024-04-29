package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Resource

data class TemplateInstance(
    val root: Resource,
    val statements: Map<ThingId, List<EmbeddedStatement>>
)
