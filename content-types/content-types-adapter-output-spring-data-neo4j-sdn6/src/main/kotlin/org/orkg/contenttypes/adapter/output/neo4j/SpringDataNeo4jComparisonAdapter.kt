package org.orkg.contenttypes.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.collect
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.size
import org.neo4j.cypherdsl.core.Cypher.toLower
import org.neo4j.cypherdsl.core.Cypher.trim
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.RelationshipPattern
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonRepository
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.PublishedVersion
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.comparisonNode
import org.orkg.graph.adapter.output.neo4j.contributionNode
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.orElseGet
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.paperNode
import org.orkg.graph.adapter.output.neo4j.query
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.domain.Classes
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

private const val RELATED = "RELATED"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_resource_on_label"

@Component
class SpringDataNeo4jComparisonAdapter(
    private val neo4jRepository: Neo4jComparisonRepository,
    private val neo4jClient: Neo4jClient
) : ComparisonRepository {
    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        published: Boolean?,
        sustainableDevelopmentGoal: ThingId?,
        researchProblem: ThingId?
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            val patterns: (Node) -> Collection<RelationshipPattern> = { node ->
                listOfNotNull(
                    researchField?.let {
                        val researchFieldNode = node(Classes.researchField).withProperties("id", anonParameter(it.value))
                        if (includeSubfields) {
                            node.relationshipTo(node(Classes.researchField), RELATED)
                                .relationshipFrom(researchFieldNode, RELATED)
                                .properties("predicate_id", literalOf<String>(Predicates.hasSubfield.value))
                                .min(0)
                        } else {
                            node.relationshipTo(researchFieldNode, RELATED)
                        }
                    },
                    doi?.let { node.relationshipTo(node("Literal").withProperties("label", anonParameter(doi))) },
                    sustainableDevelopmentGoal?.let {
                        node.relationshipTo(node("SustainableDevelopmentGoal").withProperties("id", anonParameter(it.value)), RELATED)
                            .withProperties("predicate_id", literalOf<String>(Predicates.sustainableDevelopmentGoal.value))
                    },
                    researchProblem?.let {
                        // we are not checking for predicate ids here, because the computational overhead is too high and results are expected to be almost identical
                        node.relationshipTo(node("Contribution"), RELATED)
                            .relationshipTo(node("Resource", "Problem").withProperties("id", anonParameter(it.value)), RELATED)
                    }
                )
            }
            val node = name("node")
            val nodes = name("nodes")
            val matchComparisons = matchComparison(node, patterns, published)
            val match = label?.let { searchString ->
                when (searchString) {
                    is ExactSearchString -> {
                        matchComparisons
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))).and(node.`in`(nodes)))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        matchComparisons
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)).and(node.`in`(nodes)))
                            .with(node, name("score"))
                    }
                }
            } ?: matchComparisons
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) }
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
                .returningDistinct(node)
        }
        .countDistinctOver("node")
        .mappedBy(ResourceMapper("node"))
        .fetch(pageable, false)

    override fun findVersionHistoryForPublishedComparison(id: ThingId): VersionInfo =
        neo4jRepository.findVersionHistoryForPublishedComparison(id).let { neo4jVersion ->
            VersionInfo(
                head = HeadVersion(neo4jVersion.head.toResource()),
                published = neo4jVersion.published.map { PublishedVersion(it.toResource(), null) }
                    .sortedByDescending { it.createdAt }
            )
        }

    override fun findAllDOIsRelatedToComparison(id: ThingId): Iterable<String> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val doi = name("doi")
            val node = name("node")
            call(
                union(
                    match(comparisonNode().named(node).withProperties("id", parameter("id")))
                        .returning(node)
                        .build(),
                    match(node("ComparisonPublished").named(node).withProperties("id", parameter("id")))
                        .returning(node)
                        .build()
                )
            )
                .with(node)
                .match(
                    anyNode().named(node).relationshipTo(contributionNode(), RELATED)
                        .withProperties("predicate_id", literalOf<String>(Predicates.comparesContribution.value))
                        .relationshipFrom(paperNode(), RELATED)
                        .properties("predicate_id", literalOf<String>(Predicates.hasContribution.value))
                        .relationshipTo(node("Literal").named(doi), RELATED)
                        .properties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
                )
                .returningDistinct(trim(doi.property("label")))
        }
        .withParameters("id" to id.value)
        .fetchAs<String>()
        .all()
        .filter { it.isNotBlank() }

    override fun findAllCurrentAndListedAndUnpublishedComparisons(pageable: Pageable): Page<Resource> =
        neo4jRepository.findAllCurrentListedAndUnpublishedComparisons(pageable).map { it.toResource() }
}
