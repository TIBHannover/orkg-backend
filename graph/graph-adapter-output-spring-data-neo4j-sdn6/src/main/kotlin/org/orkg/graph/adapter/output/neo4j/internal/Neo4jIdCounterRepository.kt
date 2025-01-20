package org.orkg.graph.adapter.output.neo4j.internal

import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jIdCounterRepository : Neo4jRepository<Neo4jIdCounter, String>
