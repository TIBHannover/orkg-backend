package org.orkg.contenttypes.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.size
import org.neo4j.cypherdsl.core.Functions.toLower
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.PatternElement
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.graph.adapter.output.neo4j.match
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.adapter.output.neo4j.withDefaultSort
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates

private const val RELATED = "RELATED"
private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_template_on_label"

@Component
class SpringDataNeo4jTemplateAdapter(
    private val neo4jClient: Neo4jClient
) : TemplateRepository {
    override fun findAll(
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        researchProblem: ThingId?,
        targetClassId: ThingId?,
        pageable: Pageable
    ): Page<Resource> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            val patterns: (Node) -> Collection<PatternElement> = { node ->
                listOfNotNull(
                    node.relationshipTo(
                        when (targetClassId) {
                            null -> node("Class")
                            else -> node("Class").withProperties("id", anonParameter(targetClassId.value))
                        },
                        RELATED
                    ).withProperties("predicate_id", literalOf<String>(Predicates.shTargetClass.value)),
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
                    researchProblem?.let {
                        node.relationshipTo(node("Problem").withProperties("id", anonParameter(it.value)), RELATED)
                            .withProperties("predicate_id", literalOf<String>(Predicates.templateOfResearchProblem.value))
                    }
                )
            }
            val node = name("node")
            val nodes = name("nodes")
            val matchTemplates = match(node("NodeShape").named(node), patterns)
            val match = label?.let { searchString ->
                when (searchString) {
                    is ExactSearchString -> {
                        matchTemplates
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node")
                            .where(toLower(node.property("label")).eq(toLower(anonParameter(searchString.input))).and(node.`in`(nodes)))
                            .with(node)
                    }
                    is FuzzySearchString -> {
                        matchTemplates
                            .with(collect(node).`as`(nodes))
                            .call("db.index.fulltext.queryNodes")
                            .withArgs(anonParameter(FULLTEXT_INDEX_FOR_LABEL), anonParameter(searchString.query))
                            .yield("node", "score")
                            .where(size(node.property("label")).gte(anonParameter(searchString.input.length)).and(node.`in`(nodes)))
                            .with(node, name("score"))
                    }
                }
            } ?: matchTemplates
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
}
