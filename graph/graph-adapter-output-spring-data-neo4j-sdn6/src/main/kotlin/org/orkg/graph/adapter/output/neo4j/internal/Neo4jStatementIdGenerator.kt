package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.graph.domain.StatementId
import org.springframework.stereotype.Component

@Component
class Neo4jStatementIdGenerator(
    private val repository: Neo4jStatementIdCounterRepository
) : RepositoryBasedIdGenerator<StatementId, Neo4jStatementIdCounter>() {

    override fun createCounterNode() = Neo4jStatementIdCounter()

    override fun idFromLong(value: Long) = StatementId(value)

    override fun repository() = repository
}
