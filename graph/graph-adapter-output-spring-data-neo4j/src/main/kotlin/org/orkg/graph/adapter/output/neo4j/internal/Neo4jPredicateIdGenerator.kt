package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jPredicateIdGenerator(
    repository: Neo4jIdCounterRepository,
) : RepositoryBasedIdGenerator<ThingId>("PredicateId", repository) {
    override fun idFromLong(value: Long) = ThingId("P$value")
}
