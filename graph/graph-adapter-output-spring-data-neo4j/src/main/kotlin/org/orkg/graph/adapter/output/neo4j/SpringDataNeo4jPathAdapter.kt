package org.orkg.graph.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.graph.adapter.output.neo4j.ApocConfig.CompoundRelationshipFilter
import org.orkg.graph.adapter.output.neo4j.ApocConfig.LabelFilter
import org.orkg.graph.domain.Path
import org.orkg.graph.domain.PathDirection
import org.orkg.graph.output.PathRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
@TransactionalOnNeo4j
class SpringDataNeo4jPathAdapter(
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
    private val predicateRepository: PredicateRepository,
) : PathRepository {
    override fun findAllByRootId(
        id: ThingId,
        pageable: Pageable,
        minHops: Int?,
        maxHops: Int?,
        denyClasses: Set<ThingId>,
        allowClasses: Set<ThingId>,
        terminationClasses: Set<ThingId>,
        pathDirection: PathDirection,
        includeRoot: Boolean,
    ): Page<Path> =
        cypherQueryBuilderFactory.newBuilder()
            .withCommonQuery {
                val n = name("n")
                val node = node("Thing")
                    .withProperties("id", parameter("id"))
                    .named(n)
                match(node)
                    .call("apoc.path.expandConfig")
                    .withArgs(n, parameter("config"))
                    .yield(name("path"))
            }
            .withQuery { commonQuery -> commonQuery.returning(name("path")) }
            .countOver("path")
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
                    relationshipFilter = when (pathDirection) {
                        PathDirection.OUTGOING -> CompoundRelationshipFilter.RELATED_OUTGOING
                        PathDirection.INCOMING -> CompoundRelationshipFilter.RELATED_INCOMING
                        PathDirection.UNDIRECTED -> CompoundRelationshipFilter.RELATED_UNDIRECTED
                    },
                ).toMap(),
            )
            .mappedBy(PathMapper("path", predicateRepository, includeRoot))
            .fetch(pageable)
}
