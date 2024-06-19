package org.orkg.contenttypes.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Functions
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.size
import org.neo4j.cypherdsl.core.Functions.toLower
import org.neo4j.cypherdsl.core.Predicates.exists
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jComparisonRepository
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.call
import org.orkg.graph.adapter.output.neo4j.comparisonNode
import org.orkg.graph.adapter.output.neo4j.contributionNode
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.orElseGet
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.paperNode
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toMatchOrNull
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.adapter.output.neo4j.withSortableFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

private const val RELATED = "RELATED"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_comparison_on_label"

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
        sustainableDevelopmentGoal: ThingId?
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            val node = node("Comparison").named("node")
            val nodes = name("nodes")
            val patterns = listOfNotNull(
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
                }
            )
            val match = label?.let {
                when (label) {
                    is ExactSearchString -> {
                        patterns.toMatchOrNull(node)?.with(collect(node).`as`(nodes)).call(
                            function = "db.index.fulltext.queryNodes",
                            arguments = arrayOf(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(label.query)),
                            yieldItems = arrayOf("node"),
                            condition = toLower(node.property("label")).eq(toLower(anonParameter(label.input))),
                        ).with(node)
                    }
                    is FuzzySearchString -> {
                        patterns.toMatchOrNull(node)?.with(collect(node).`as`(nodes)).call(
                            function = "db.index.fulltext.queryNodes",
                            arguments = arrayOf(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(label.query)),
                            yieldItems = arrayOf("node", "score"),
                            condition = size(node.property("label")).gte(anonParameter(label.input.length))
                        ).with(node, name("score"))
                    }
                }
            } ?: patterns.toMatchOrNull(node)?.with(node) ?: match(node).with(node)
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: Conditions.noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) },
                if (label != null && patterns.isNotEmpty()) node.asExpression().`in`(nodes) else Conditions.noCondition(),
                if (doi != null || label != null || createdBy != null) {
                    Conditions.noCondition()
                } else {
                    exists(
                        node("Comparison").relationshipTo(node, RELATED)
                            .withProperties("predicate_id", literalOf<String>(Predicates.hasPreviousVersion.value))
                    ).not()
                }
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

    override fun findVersionHistory(id: ThingId): List<HeadVersion> =
        neo4jRepository.findVersionHistory(id)

    override fun findAllDOIsRelatedToComparison(id: ThingId): Iterable<String> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val doi = name("doi")
            val relations = comparisonNode()
                .withProperties("id", Cypher.parameter("id"))
                .relationshipTo(contributionNode(), RELATED)
                .withProperties("predicate_id", literalOf<String>(Predicates.comparesContribution.value))
                .relationshipFrom(paperNode(), RELATED)
                .properties("predicate_id", literalOf<String>(Predicates.hasContribution.value))
                .relationshipTo(node("Literal").named(doi), RELATED)
                .properties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
            match(relations).returningDistinct(Functions.trim(doi.property("label")))
        }
        .withParameters("id" to id.value)
        .fetchAs<String>()
        .all()
        .filter { it.isNotBlank() }

    override fun findAllCurrentListedAndUnpublishedComparisons(pageable: Pageable): Page<Resource> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val cmp = comparisonNode().named("node")
            match(cmp).where(
                exists(
                    comparisonNode().relationshipTo(cmp, "RELATED")
                        .withProperties("predicate_id", literalOf<String>(Predicates.hasPreviousVersion.value))
                ).not()
                    .and(
                        cmp.property("visibility").eq(literalOf<String>("DEFAULT"))
                            .or(cmp.property("visibility").eq(literalOf<String>("FEATURED")))
                    )
                    .and(
                        exists(
                            cmp.relationshipTo(node("Literal"), "RELATED")
                                .withProperties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
                        ).not()
                    )
            )
        }
        .withQuery { commonQuery ->
            commonQuery.withSortableFields("node")
                .orderBy(Cypher.sort(name("created_at")))
                .returning("node")
        }
        .countOver("node")
        .mappedBy(ResourceMapper("node"))
        .fetch(pageable)

    @Deprecated("To be removed", replaceWith = ReplaceWith("findAll"))
    override fun findAllListedComparisonsByResearchField(
        id: ThingId,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllListedComparisonsByResearchFieldIncludingSubFields(id, pageable)
            false -> neo4jRepository.findAllListedComparisonsByResearchFieldExcludingSubFields(id, pageable)
        }.map { it.toResource() }

    @Deprecated("To be removed", replaceWith = ReplaceWith("findAll"))
    override fun findAllComparisonsByResearchFieldAndVisibility(
        id: ThingId,
        visibility: Visibility,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (includeSubfields) {
            true -> neo4jRepository.findAllComparisonsByResearchFieldAndVisibilityIncludingSubFields(id, visibility, pageable)
            false -> neo4jRepository.findAllComparisonsByResearchFieldAndVisibilityExcludingSubFields(id, visibility, pageable)
        }.map { it.toResource() }
}
