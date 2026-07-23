package org.orkg.graph.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.relationships
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.withFallbackSort
import org.orkg.graph.adapter.output.neo4j.ApocConfig.LabelFilter
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.SubgraphRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
@TransactionalOnNeo4j
class SpringDataNeo4jSubgraphAdapter(
    private val predicateRepository: PredicateRepository,
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : SubgraphRepository {
    override fun findByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int?,
        maxHops: Int?,
        denyClasses: Set<ThingId>,
        allowClasses: Set<ThingId>,
        terminationClasses: Set<ThingId>,
    ): Page<GeneralStatement> =
        cypherQueryBuilderFactory.newBuilder()
            .withCommonQuery {
                val n = name("n")
                val node = node("Thing")
                    .withProperties("id", parameter("id"))
                    .named(n)
                val path = name("path")
                val rel = name("rel")
                match(node)
                    .call("apoc.path.expandConfig")
                    .withArgs(n, parameter("config"))
                    .yield(path)
                    .with(path)
                    .unwind(relationships(path))
                    .`as`(rel)
                    .withDistinct(rel)
            }
            .withQuery { commonQuery ->
                val rel = name("rel")
                commonQuery
                    .with(startNode(rel).`as`("sub"), rel.`as`("rel"), endNode(rel).`as`("obj"))
                    .returningStatementWithSortableFields("rel", "sub", "obj")
            }
            .countOver("rel")
            .withParameters(
                "id" to id.value,
                "config" to ApocConfig(
                    minLevel = minHops ?: 1,
                    maxLevel = maxHops ?: 10,
                    labelFilter = LabelFilter(
                        denyLabels = denyClasses,
                        allowLabels = allowClasses,
                        terminationLabels = terminationClasses,
                    ),
                ).toMap(),
            )
            .mappedBy(StatementMapper(predicateRepository))
            .fetch(pageable.withFallbackSort(Sort.by("created_at", "id")))
}
