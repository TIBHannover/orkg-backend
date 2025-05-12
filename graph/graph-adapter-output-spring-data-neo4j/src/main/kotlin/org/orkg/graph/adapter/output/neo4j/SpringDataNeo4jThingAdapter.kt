package org.orkg.graph.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.size
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThing
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThingRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ThingRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

const val THING_ID_TO_THING_CACHE = "thing-id-to-thing"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_thing_on_label"

@Component
@TransactionalOnNeo4j
@CacheConfig(cacheNames = [THING_ID_TO_THING_CACHE])
class SpringDataNeo4jThingAdapter(
    private val neo4jRepository: Neo4jThingRepository,
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : ThingRepository {
    @Cacheable(key = "#id", cacheNames = [THING_ID_TO_THING_CACHE])
    override fun findById(id: ThingId): Optional<Thing> = neo4jRepository.findById(id).map(Neo4jThing::toThing)

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
    ): Page<Thing> = cypherQueryBuilderFactory.newBuilder(Uncached)
        .withCommonQuery {
            val node = node("Thing", includeClasses.map { it.value }).named("node")
            val match = label?.let { searchString ->
                val labelCondition = includeClasses.toCondition {
                    node.hasLabels(*includeClasses.map { it.value }.toTypedArray())
                }
                when (searchString) {
                    is ExactSearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))).and(labelCondition))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)).and(labelCondition))
                            .with(node, name("score"))
                    }
                }
            } ?: match(node).with(node)
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                excludeClasses.toCondition { classes -> classes.map { node.hasLabels(it.value).not() }.reduce(Condition::and) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) },
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val score = if (label != null && label is FuzzySearchString) name("score") else null
            val variables = listOfNotNull(node, score)
            val sort = pageable.sort.orElseGet { Sort.by("created_at") }
            commonQuery
                .with(variables) // "with" is required because cypher dsl reorders "orderBy" and "where" clauses sometimes, decreasing performance
                .where(
                    orderByOptimizations(
                        node = node,
                        sort = sort,
                        properties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                    )
                )
                .with(variables)
                .orderBy(
                    if (score != null) {
                        listOf(
                            size(node.property("label")).ascending(),
                            score.descending(),
                            node.property("created_at").ascending()
                        )
                    } else {
                        sort.toSortItems(
                            node = node,
                            knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                        )
                    }
                )
                .returning(node)
        }
        .countOver("node")
        .mappedBy(ThingMapper("node"))
        .fetch(pageable, false)

    override fun existsAllById(ids: Set<ThingId>): Boolean = neo4jRepository.existsAll(ids)

    override fun isUsedAsObject(id: ThingId): Boolean = neo4jRepository.isUsedAsObject(id)
}
