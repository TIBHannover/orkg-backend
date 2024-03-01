package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.adapter.output.neo4j.withDefaultSort
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.neo4j.cypherdsl.core.Cypher.*
import org.neo4j.cypherdsl.core.Functions.*

private const val RELATED = "RELATED"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_resource_on_label"

@Component
class SpringDataNeo4jSmartReviewAdapter(
    private val neo4jClient: Neo4jClient
) : SmartReviewRepository {
    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        published: Boolean?
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            val node = name("node")
            val nodes = name("nodes")
            val matchSmartReviews = when (published) {
                true -> matchPublishedSmartReviews(node)
                false -> matchUnpublishedSmartReviews(node)
                else -> call(
                    unionAll(
                        matchPublishedSmartReviews(node).returning(node).build(),
                        matchUnpublishedSmartReviews(node).returning(node).build()
                    )
                ).with(node)
            }
            val match = label?.let { searchString ->
                when (searchString) {
                    is ExactSearchString -> {
                        matchSmartReviews
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))).and(node.`in`(nodes)))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        matchSmartReviews
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)).and(node.`in`(nodes)))
                            .with(node, name("score"))
                    }
                }
            } ?: matchSmartReviews
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: Conditions.noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) }
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val score = if (label != null && label is FuzzySearchString) name("score") else null
            val variables = listOfNotNull(node, score)
            val pageableWithDefaultSort = pageable.withDefaultSort { Sort.by("created_at") }
            commonQuery
                .with(variables) // "with" is required because cypher dsl reorders "orderBy" and "where" clauses sometimes, decreasing performance
                .where(
                    orderByOptimizations(
                        node = node,
                        sort = pageableWithDefaultSort.sort,
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
                        pageableWithDefaultSort.sort.toSortItems(
                            node = node,
                            knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                        )
                    }
                )
                .returning(node)
        }
        .countOver("node")
        .mappedBy(ResourceMapper("node"))
        .fetch(pageable, false)

    private fun matchPublishedSmartReviews(symbolicName: SymbolicName): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere {
        val srp = name("srp")
        val srl = name("srl")
        return match(
            node("SmartReviewPublished").named(srp)
                .relationshipFrom(node("SmartReview").named(srl), RELATED)
                .withProperties("predicate_id", literalOf<String>(Predicates.hasPublishedVersion.value))
        ).with(
            srl.asExpression(),
            valueAt(call("apoc.coll.sortNodes").withArgs(collect(srp), literalOf<String>("created_at")).asFunction(), 0).`as`(symbolicName)
        )
    }

    private fun matchUnpublishedSmartReviews(symbolicName: SymbolicName): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
        match(node("SmartReview").named(symbolicName)).with(symbolicName)
}
