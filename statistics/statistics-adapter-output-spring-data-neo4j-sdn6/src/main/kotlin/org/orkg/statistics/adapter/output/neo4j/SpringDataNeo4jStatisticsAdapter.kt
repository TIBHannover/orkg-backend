package org.orkg.statistics.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.count
import org.neo4j.cypherdsl.core.Cypher.exists
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.node
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.statistics.output.StatisticsRepository
import org.springframework.stereotype.Component

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jStatisticsAdapter(
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory
) : StatisticsRepository {
    override fun countNodes(label: String): Long =
        cypherQueryBuilderFactory.newBuilder(Uncached)
            .withQuery {
                val node = node(label)
                match(node).returning(count(node))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)

    override fun countRelations(type: String): Long =
        cypherQueryBuilderFactory.newBuilder(Uncached)
            .withQuery {
                val rel = node("Thing").relationshipTo(node("Thing"), type)
                match(rel).returning(count(rel.asExpression()))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)

    override fun countUnusedNodes(label: String): Long =
        cypherQueryBuilderFactory.newBuilder(Uncached)
            .withQuery {
                val node = node(label)
                match(node)
                    .where(exists(node.relationshipFrom(anyNode(), RELATED)).not())
                    .returning(count(node))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)

    override fun countOrphanNodes(label: String): Long =
        cypherQueryBuilderFactory.newBuilder(Uncached)
            .withQuery {
                val node = node(label)
                match(node)
                    .where(exists(node.relationshipBetween(anyNode(), RELATED)).not())
                    .returning(count(node))
            }
            .fetchAs<Long>()
            .one()
            .orElse(0)
}
