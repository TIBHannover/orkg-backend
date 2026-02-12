package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.exists
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.IdentifiableElement
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.node
import org.orkg.graph.adapter.output.neo4j.orElseGet
import org.orkg.graph.adapter.output.neo4j.orderByOptimizations
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jResearchProblemAdapter(
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : ResearchProblemRepository {
    override fun findAll(
        pageable: Pageable,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        addressedByObservatory: ObservatoryId?,
        addressedByOrganization: OrganizationId?,
    ): Page<Resource> =
        buildFindAllQuery(
            sort = pageable.sort.orElseGet { Sort.by("created_at") },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            addressedByObservatory = addressedByObservatory,
            addressedByOrganization = addressedByOrganization,
        ).fetch(pageable, false)

    override fun count(
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        addressedByObservatory: ObservatoryId?,
        addressedByOrganization: OrganizationId?,
    ): Long =
        buildFindAllQuery(
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            addressedByObservatory = addressedByObservatory,
            addressedByOrganization = addressedByOrganization,
        ).count()

    private fun buildFindAllQuery(
        sort: Sort = Sort.unsorted(),
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        addressedByObservatory: ObservatoryId?,
        addressedByOrganization: OrganizationId?,
    ) = cypherQueryBuilderFactory.newBuilder(QueryCache.Uncached)
        .withCommonQuery {
            val node = node(Classes.problem).named("node")
            val researchFieldNodeName = name("field")
            val includeResearchFieldPattern = researchField != null || sort.get().anyMatch { it.property == "research_field_count" }
            val match = if (includeResearchFieldPattern) {
                val researchFieldNode = researchField
                    ?.let { node(Classes.researchField).named(researchFieldNodeName).withProperties("id", anonParameter(it.value)) }
                    ?: node(Classes.researchField).named(researchFieldNodeName)
                val path = node.relationshipFrom(node(Classes.contribution), RELATED)
                    .relationshipFrom(node(Classes.paper), RELATED)
                val researchFieldPattern = if (includeSubfields) {
                    path.relationshipTo(node(Classes.researchField))
                        .relationshipFrom(researchFieldNode)
                        .properties("predicate_id", literalOf<String>(Predicates.hasSubfield.value))
                        .min(0)
                } else {
                    path.relationshipTo(researchFieldNode, RELATED)
                }
                val match = researchField
                    ?.let { match(node).match(researchFieldPattern) }
                    ?: match(node).optionalMatch(researchFieldPattern)
                match.with(node, Cypher.count(researchFieldNodeName).`as`("research_field_count"))
            } else {
                match(node).with(node)
            }
            match.where(
                visibility.toCondition { filter ->
                    filter.targets.map { node.property("visibility").eq(literalOf<String>(it.name)) }
                        .reduceOrNull(Condition::or) ?: noCondition()
                },
                createdBy.toCondition { node.property("created_by").eq(anonParameter(it.value.toString())) },
                createdAtStart.toCondition { node.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                createdAtEnd.toCondition { node.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                observatoryId.toCondition { node.property("observatory_id").eq(anonParameter(it.value.toString())) },
                organizationId.toCondition { node.property("organization_id").eq(anonParameter(it.value.toString())) },
                // in some scenarios, it might be beneficial to move this check to the match pattern expression
                addressedByObservatory.toCondition {
                    exists(
                        node.relationshipFrom(node(Classes.contribution))
                            .relationshipFrom(
                                node(Classes.thing).withProperties("observatory_id", literalOf<String>(it.value.toString()))
                            )
                    )
                },
                // in some scenarios, it might be beneficial to move this check to the match pattern expression
                addressedByOrganization.toCondition {
                    exists(
                        node.relationshipFrom(node(Classes.contribution))
                            .relationshipFrom(
                                node(Classes.thing).withProperties("organization_id", literalOf<String>(it.value.toString()))
                            )
                    )
                },
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val variables = mutableListOf<IdentifiableElement>(node)
            val includeResearchFieldPattern = researchField != null || sort.get().anyMatch { it.property == "research_field_count" }
            if (includeResearchFieldPattern) {
                variables += name("research_field_count")
            }
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
                    sort.toSortItems(
                        propertyMappings = listOf("id", "label", "created_at", "created_by", "visibility").associateWith { node.property(it) },
                        knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility", "research_field_count")
                    )
                )
                .returningDistinct(node)
        }
        .countDistinctOver("node")
        .mappedBy(ResourceMapper("node"))
}
