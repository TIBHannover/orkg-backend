package org.orkg.contenttypes.adapter.output.neo4j

import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.countDistinct
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.IdentifiableElement
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilderFactory
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.output.ResearchFieldRepository
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
class SpringDataNeo4jResearchFieldAdapter(
    private val cypherQueryBuilderFactory: CypherQueryBuilderFactory,
) : ResearchFieldRepository {
    override fun findAll(
        pageable: Pageable,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchProblem: ThingId?,
        includeSubproblems: Boolean,
    ): Page<Resource> =
        buildFindAllQuery(
            sort = pageable.sort.orElseGet { Sort.by("created_at") },
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchProblem = researchProblem,
            includeSubproblems = includeSubproblems,
        ).fetch(pageable, false)

    override fun count(
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchProblem: ThingId?,
        includeSubproblems: Boolean,
    ): Long =
        buildFindAllQuery(
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchProblem = researchProblem,
            includeSubproblems = includeSubproblems,
        ).count()

    private fun buildFindAllQuery(
        sort: Sort = Sort.unsorted(),
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchProblem: ThingId?,
        includeSubproblems: Boolean,
    ) = cypherQueryBuilderFactory.newBuilder(QueryCache.Uncached)
        .withCommonQuery {
            val node = node("ResearchField").named("node")
            val researchProblemNodeName = name("problem")
            val includeResearchProblemPattern = researchProblem != null || sort.get().anyMatch { it.property == "research_problem_count" }
            val match = if (includeResearchProblemPattern) {
                val researchProblemNode = researchProblem
                    ?.let { node(Classes.problem).named(researchProblemNodeName).withProperties("id", anonParameter(it.value)) }
                    ?: node(Classes.problem).named(researchProblemNodeName)
                val path = node.relationshipFrom(node(Classes.paper), RELATED)
                    .relationshipTo(node(Classes.contribution), RELATED)
                val researchProblemPattern = if (includeSubproblems) {
                    path.relationshipTo(node(Classes.problem))
                        .relationshipFrom(researchProblemNode)
                        .properties("predicate_id", literalOf<String>(Predicates.subProblem.value))
                        .min(0)
                } else {
                    path.relationshipTo(researchProblemNode, RELATED)
                }
                val match = researchProblem
                    ?.let { match(node).match(researchProblemPattern) }
                    ?: match(node).optionalMatch(researchProblemPattern)
                match.with(node, countDistinct(researchProblemNodeName).`as`("research_problem_count"))
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
            )
        }
        .withQuery { commonQuery ->
            val node = name("node")
            val variables = mutableListOf<IdentifiableElement>(node)
            val includeResearchProblemPattern = researchProblem != null || sort.get().anyMatch { it.property == "research_problem_count" }
            if (includeResearchProblemPattern) {
                variables += name("research_problem_count")
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
                        knownProperties = arrayOf("id", "label", "created_at", "created_by", "visibility", "research_problem_count")
                    )
                )
                .returningDistinct(node)
        }
        .countDistinctOver("node")
        .mappedBy(ResourceMapper("node"))
}
