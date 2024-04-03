package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.adapter.output.neo4j.withDefaultSort
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.neo4j.cypherdsl.core.Cypher.*
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.Predicates.*
import org.neo4j.cypherdsl.core.RelationshipPattern
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.contenttypes.output.ContentTypeRepository
import org.orkg.graph.adapter.output.neo4j.match

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jContentTypeAdapter(
    private val neo4jClient: Neo4jClient
) : ContentTypeRepository {
    override fun findAll(
        pageable: Pageable,
        classes: Set<ContentTypeClass>,
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
            val node = name("node")
            val matches = classes.map { contentType ->
                val patterns: (Node) -> List<RelationshipPattern> = { node ->
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
                        sustainableDevelopmentGoal?.let {
                            node.relationshipTo(node("SustainableDevelopmentGoal").withProperties("id", anonParameter(it.value)), RELATED)
                                .withProperties("predicate_id", literalOf<String>(Predicates.sustainableDevelopmentGoal.value))
                        }
                    )
                }
                contentType.match(node, patterns)
                    .where(
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
                    .returning(node)
                    .build()
            }
            call(if (matches.size > 1) unionAll(matches) else matches.first()).with(node)
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val pageableWithDefaultSort = pageable.withDefaultSort { Sort.by("created_at") }
            commonQuery.orderBy(
                    pageableWithDefaultSort.sort.toSortItems(
                        node = node,
                        knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility")
                    )
                )
                .returningDistinct(node)
        }
        .countDistinctOver("node")
        .mappedBy(ResourceMapper("node"))
        .fetch(pageable, false)

    private fun ContentTypeClass.match(
        node: SymbolicName,
        patternGenerator: (Node) -> Collection<RelationshipPattern>
    ): StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere =
        when (this) {
            ContentTypeClass.PAPER -> match(node("Paper").named(node), patternGenerator)
            ContentTypeClass.COMPARISON -> {
                val comparison = node("Comparison").named(node)
                match(comparison, patternGenerator)
                    .where(
                        exists(
                            node("Comparison").relationshipTo(comparison, RELATED)
                                .withProperties("predicate_id", literalOf<String>(Predicates.hasPreviousVersion.value))
                        ).not()
                    )
                    .with(node)
            }
            ContentTypeClass.VISUALIZATION -> match(node("Visualization").named(node), patternGenerator)
            ContentTypeClass.TEMPLATE -> match(node("NodeShape").named(node), patternGenerator)
            ContentTypeClass.LITERATURE_LIST -> matchLiteratureList(node, patternGenerator)
            ContentTypeClass.SMART_REVIEW -> matchSmartReview(node, patternGenerator)
        }
}
