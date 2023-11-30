package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.sortedWith
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.adapter.output.neo4j.ResourceMapper
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jFormattedLabelRepository
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

private const val FULLTEXT_INDEX_FOR_LABEL = "fulltext_idx_for_template_on_label"

@Component
class SpringDataNeo4jTemplateAdapter(
    private val neo4jRepository: Neo4jFormattedLabelRepository,
    private val neo4jClient: Neo4jClient
) : TemplateRepository {
    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        researchFieldId: ThingId?,
        researchProblemId: ThingId?,
        targetClassId: ThingId?,
        pageable: Pageable
    ): Page<Resource> {
        val match = when (searchString) {
            null -> """MATCH (template:NodeShape)"""
            else -> buildString {
                append("""CALL db.index.fulltext.queryNodes("$FULLTEXT_INDEX_FOR_LABEL", ${'$'}query) YIELD node, score """)
                append(
                    when (searchString) {
                        is ExactSearchString -> """WHERE toLower(node.label) = toLower(${'$'}label)"""
                        is FuzzySearchString -> """WHERE SIZE(node.label) >= ${'$'}minLabelLength"""
                    }
                )
                append(""" WITH node AS template, score""")
            }
        }
        val where = buildString {
            if (visibility != null) {
                append(
                    visibility.targets.joinToString(
                        prefix = " AND (",
                        separator = " OR ",
                        postfix = ")"
                    ) { """template.visibility = "$it"""" }
                )
            }
            if (createdBy != null) {
                append(""" AND template.created_by = ${'$'}createdBy""")
            }
            if (researchFieldId != null) {
                append(""" AND EXISTS((template)-[:RELATED {predicate_id: "TemplateOfResearchField"}]->(:ResearchField {id: ${'$'}researchFieldId}))""")
            }
            if (researchProblemId != null) {
                append(""" AND EXISTS((template)-[:RELATED {predicate_id: "TemplateOfResearchProblem"}]->(:Problem {id: ${'$'}researchProblemId}))""")
            }
            if (targetClassId != null) {
                append(""" AND EXISTS((template)-[:RELATED {predicate_id: "sh:targetClass"}]->(:Class {id: ${'$'}targetClassId}))""")
            }
        }.replaceFirst(" AND", "WHERE")
        val with = when (searchString) {
            is FuzzySearchString -> """WITH template, template.id AS id, template.label AS label, template.created_at AS created_at, score"""
            else -> """WITH template, template.id AS id, template.label AS label, template.created_at AS created_at"""
        }
        val sort = when (searchString) {
            is FuzzySearchString -> Sort.by(Direction.ASC, "SIZE(label)")
                .and(Sort.by(Direction.DESC, "score"))
                .and(Sort.by(Direction.DESC, "created_at"))
            else -> if (pageable.sort.isSorted) pageable.sort else Sort.by(Direction.ASC, "created_at")
        }
        val commonQuery = "$match $where"
        val query = "$commonQuery $with RETURN DISTINCT template SKIP ${'$'}skip LIMIT ${'$'}limit".let {
            it.sortedWith(sort, it.lastIndexOf("RETURN"))
        }
        val countQuery = "$commonQuery RETURN COUNT(DISTINCT template)"
        val parameters = mapOf(
            "query" to searchString?.query,
            "label" to searchString?.input,
            "minLabelLength" to searchString?.input?.length,
            "createdBy" to createdBy?.value?.toString(),
            "researchFieldId" to researchFieldId?.value,
            "researchProblemId" to researchProblemId?.value,
            "targetClassId" to targetClassId?.value,
            "skip" to pageable.offset,
            "limit" to pageable.pageSize
        )
        val elements = neo4jClient.query(query)
            .bindAll(parameters)
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper("template"))
            .all()
        val count = neo4jClient.query(countQuery)
            .bindAll(parameters)
            .fetchAs(Long::class.java)
            .one()
            .orElse(0)
        return PageImpl(elements.toList(), pageable, count)
    }
}
