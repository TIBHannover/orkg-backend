package org.orkg.graph.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, ThingId>
