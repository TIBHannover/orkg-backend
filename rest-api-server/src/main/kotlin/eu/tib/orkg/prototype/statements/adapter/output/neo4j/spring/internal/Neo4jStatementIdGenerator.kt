package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RepositoryBasedIdGenerator
import org.springframework.stereotype.Component

@Component
class Neo4jStatementIdGenerator(
    private val repository: Neo4jStatementIdCounterRepository
) : RepositoryBasedIdGenerator<StatementId, Neo4jStatementIdCounter>() {

    override fun createCounterNode() = Neo4jStatementIdCounter()

    override fun idFromLong(value: Long) = StatementId(value)

    override fun repository() = repository
}
