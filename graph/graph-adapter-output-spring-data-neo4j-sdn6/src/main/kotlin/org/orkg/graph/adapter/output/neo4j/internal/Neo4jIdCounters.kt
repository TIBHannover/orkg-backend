package org.orkg.graph.adapter.output.neo4j.internal

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.repository.Neo4jRepository

sealed class Neo4jCounter {
    @Id
    @GeneratedValue
    private var id: Long? = null

    var counter: Long = 0L
}

interface Neo4jIdCounterRepository<T : Neo4jCounter> : Neo4jRepository<T, Long>
