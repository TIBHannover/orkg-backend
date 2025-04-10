package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource

data class TemplateInstance(
    val root: Resource,
    val predicates: Map<ThingId, Predicate>,
    val statements: Map<ThingId, List<EmbeddedStatement>>,
)
