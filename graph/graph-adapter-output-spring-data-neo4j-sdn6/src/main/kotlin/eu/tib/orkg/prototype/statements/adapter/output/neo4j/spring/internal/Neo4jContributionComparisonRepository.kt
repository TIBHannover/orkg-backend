package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val ids = "${'$'}ids"
private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jContributionComparisonRepository : Neo4jRepository<Neo4jResource, Long> {
    @Query(value = """UNWIND $ids AS id
MATCH (cont:Contribution:Resource {id: id})<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (p)-[:RELATED {predicate_id: 'P29'}]->(year:Thing)
WITH cont.id AS contributionId, cont.label AS label, p.label AS title, year.label AS year, p.id AS paperId
RETURN contributionId, label, title, year, paperId $PAGE_PARAMS""",
        countQuery = """UNWIND $ids AS id MATCH (r:Resource {id: id}) RETURN COUNT(r) AS cnt""")
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<Neo4jContributionInfo>
}


data class Neo4jContributionInfo(
    val contributionId: ThingId,
    val label: String,
    val title: String,
    val year: String?,
    val paperId: ThingId
) {
    fun toContributionInfo() =
        ContributionInfo(
            id = contributionId,
            label = label,
            paperTitle = title,
            paperYear = year?.toInt(),
            paperId = paperId
        )
}