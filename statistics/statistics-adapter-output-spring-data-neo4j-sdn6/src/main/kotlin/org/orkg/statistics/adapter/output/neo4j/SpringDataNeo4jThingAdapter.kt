package org.orkg.statistics.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Functions.count
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.statistics.adapter.output.neo4j.internal.Neo4jStatisticsRepository
import org.orkg.statistics.output.StatisticsRepository
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jStatisticsAdapter(
    private val neo4jRepository: Neo4jStatisticsRepository,
    private val neo4jClient: Neo4jClient
) : StatisticsRepository {
    override fun countNodes(label: String): Long =
        CypherQueryBuilder(neo4jClient, Uncached)
            .withQuery {
                val node = node(label)
                match(node).returning(count(node))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)

    override fun countRelations(type: String): Long =
        CypherQueryBuilder(neo4jClient, Uncached)
            .withQuery {
                val rel = node("Thing").relationshipTo(node("Thing"), type)
                match(rel).returning(count(rel.asExpression()))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)
}
