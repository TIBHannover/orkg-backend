package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jClassIdGenerator(
    private val repository: Neo4jClassIdCounterRepository
) : RepositoryBasedIdGenerator<ThingId, Neo4jClassIdCounter>() {

    override fun createCounterNode() = Neo4jClassIdCounter()

    override fun idFromLong(value: Long) = ThingId("C$value")

    override fun repository() = repository
}