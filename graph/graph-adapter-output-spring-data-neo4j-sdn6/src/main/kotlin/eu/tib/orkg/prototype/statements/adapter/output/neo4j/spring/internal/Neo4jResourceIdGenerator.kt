package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.stereotype.Component

@Component
class Neo4jResourceIdGenerator(
    private val repository: Neo4jResourceIdCounterRepository
) :
    RepositoryBasedIdGenerator<ThingId, Neo4jResourceIdCounter>() {

    override fun createCounterNode() = Neo4jResourceIdCounter()

    override fun idFromLong(value: Long) = ThingId("R$value")

    override fun repository() = repository
}
