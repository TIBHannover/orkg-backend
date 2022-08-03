package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity("_StatementIdCounter")
data class Neo4jStatementIdCounter(
    @Id
    @GeneratedValue
    private var id: Long? = null
) : Neo4jCounter()
