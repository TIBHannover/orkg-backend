package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jPredicateIdGenerator(
    private val repository: Neo4jPredicateIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jPredicateIdCounter>() {

    override fun createCounterNode() = Neo4jPredicateIdCounter()

    override fun idFromLong(value: Long) = ThingId("P$value")

    override fun repository() = repository
}
