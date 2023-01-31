package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import org.springframework.stereotype.Component

@Component
class Neo4jLiteralIdGenerator(
    private val repository: Neo4jLiteralIdCounterRepository
) : RepositoryBasedIdGenerator<LiteralId, Neo4jLiteralIdCounter>() {

    override fun createCounterNode() = Neo4jLiteralIdCounter()

    override fun idFromLong(value: Long) = LiteralId(value)

    override fun repository() = repository
}
