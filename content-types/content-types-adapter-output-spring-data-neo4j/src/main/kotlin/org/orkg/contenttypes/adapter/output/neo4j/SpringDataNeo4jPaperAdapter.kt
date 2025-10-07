package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.collect
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
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jPaperRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jPaperWithPath
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.call
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicate
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThing
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.orElseGet
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.query
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toMatchOrNull
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

private const val RELATED = "RELATED"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_paper_on_label"

@Component
class SpringDataNeo4jPaperAdapter(
    private val neo4jRepository: Neo4jPaperRepository,
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : PaperRepository {
    override fun findAllPapersRelatedToResource(id: ThingId, pageable: Pageable): Page<PaperResourceWithPath> =
        neo4jRepository.findAllPapersRelatedToResource(id, pageable)
            .map { it.toPaperResourceWithPath() }

    override fun findAll(
        pageable: Pageable,
        label: SearchString?,
        doi: String?,
        doiPrefix: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?,
        mentionings: Set<ThingId>?,
        researchProblem: ThingId?,
    ): Page<Resource> =
        buildFindAllQuery(
            sort = pageable.sort.orElseGet { Sort.by("created_at") },
            label = label,
            doi = doi,
            doiPrefix = doiPrefix,
            visibility = visibility,
            verified = verified,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            mentionings = mentionings,
            researchProblem = researchProblem,
        ).fetch(pageable, false)

    override fun count(
        label: SearchString?,
        doi: String?,
        doiPrefix: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?,
        mentionings: Set<ThingId>?,
        researchProblem: ThingId?,
    ): Long =
        buildFindAllQuery(
            label = label,
            doi = doi,
            doiPrefix = doiPrefix,
            visibility = visibility,
            verified = verified,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            mentionings = mentionings,
            researchProblem = researchProblem,
        ).count()

    private fun buildFindAllQuery(
        sort: Sort = Sort.unsorted(),
        label: SearchString?,
        doi: String?,
        doiPrefix: String?,
        visibility: VisibilityFilter?,
        verified: Boolean?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?,
        mentionings: Set<ThingId>?,
        researchProblem: ThingId?,
    ) = cypherQueryBuilderFactory.newBuilder(QueryCache.Uncached)
        .withCommonQuery {
            val node = node("Paper").named("node")
            val nodes = name("nodes")
            val doiLiteral = name("doi")
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
                if (doi != null || doiPrefix != null) node.relationshipTo(node("Literal").named(doiLiteral), RELATED) else null,
                sustainableDevelopmentGoal?.let {
                    node.relationshipTo(node(Classes.sustainableDevelopmentGoal).withProperties("id", anonParameter(it.value)), RELATED)
                        .withProperties("predicate_id", literalOf<String>(Predicates.sustainableDevelopmentGoal.value))
                },
                *mentionings?.map {
                    node.relationshipTo(node("Resource").withProperties("id", anonParameter(it.value)), RELATED)
                        .withProperties("predicate_id", literalOf<String>(Predicates.mentions.value))
                }.orEmpty().toTypedArray(),
                researchProblem?.let {
                    // we are not checking for predicate ids here, because the computational overhead is too high and results are expected to be almost identical
                    node.relationshipTo(node(Classes.contribution), RELATED)
                        .relationshipTo(node(Classes.problem).withProperties("id", anonParameter(it.value)), RELATED)
                },
            )
            val patternBoundWhere = listOf(
                doi.toCondition { toLower(doiLiteral.property("label")).eq(anonParameter(doi)) },
                doiPrefix.toCondition { toLower(doiLiteral.property("label")).startsWith(anonParameter(it.lowercase().dropLastWhile { c -> c == '/' } + '/')) }
            ).reduceOrNull(Condition::and) ?: noCondition()
            val match = label?.let {
                when (label) {
                    is ExactSearchString -> {
                        patterns.toMatchOrNull(node)
                            ?.where(patternBoundWhere)
                            ?.with(collect(node).`as`(nodes))
                            .call(
                                function = "db.index.fulltext.queryNodes",
                                arguments = arrayOf(
                                    anonParameter(FULLTEXT_INDEX_FOR_LABEL),
                                    anonParameter(label.query)
                                ),
                                yieldItems = arrayOf("node"),
                                condition = toLower(node.property("label")).eq(toLower(anonParameter(label.input)))
                            )
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        patterns.toMatchOrNull(node)
                            ?.where(patternBoundWhere)
                            ?.with(collect(node).`as`(nodes))
                            .call(
                                function = "db.index.fulltext.queryNodes",
                                arguments = arrayOf(
                                    anonParameter(FULLTEXT_INDEX_FOR_LABEL),
                                    anonParameter(label.query)
                                ),
                                yieldItems = arrayOf("node", "score"),
                                condition = size(node.property("label")).gte(anonParameter(label.input.length))
                            )
                            .with(node, name("score"))
                    }
                }
            }
                ?: patterns.toMatchOrNull(node)?.where(patternBoundWhere)?.with(node)
                ?: match(node).with(node)
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: noCondition()
                },
                verified.toCondition {
                    if (it) {
                        node.property("verified").eq(literalOf<Boolean>(true))
                    } else {
                        node.property("verified").eq(literalOf<Boolean>(false)).or(node.property("verified").isNull)
                    }
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) },
                if (label != null && patterns.isNotEmpty()) node.asExpression().`in`(nodes) else noCondition()
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val score = if (label != null && label is FuzzySearchString) name("score") else null
            val variables = listOfNotNull(node, score)
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
}

fun Neo4jPaperWithPath.toPaperResourceWithPath() =
    PaperResourceWithPath(
        paper.toResource(),
        aggregateAndConvertToModelObjects(paper.id!!, path)
    )

private fun aggregateAndConvertToModelObjects(paperId: ThingId, path: Iterable<Neo4jThing>): List<List<Thing>> {
    val finalResult = mutableListOf<List<Thing>>()
    var possiblePath = mutableListOf<Thing>()
    for (p in path) {
        if (p.id!! == paperId) {
            possiblePath = mutableListOf()
            finalResult.add(possiblePath)
        }
        when (p) {
            is Neo4jResource, is Neo4jPredicate -> possiblePath.add(p.toThing())
            else -> throw IllegalStateException("Result types can only be either resources or predicates. This is a bug!")
        }
    }
    return finalResult
}
