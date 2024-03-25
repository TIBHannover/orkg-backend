package org.orkg.statistics.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jStatisticsRepository : Neo4jRepository<Neo4jResource, ThingId>
