package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val ids = "${'$'}ids"

interface Neo4jContributionComparisonRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""UNWIND $ids AS id
MATCH (cont:Contribution:Resource {id: id})<-[:RELATED {predicate_id: 'P31'}]-(p:Paper)
OPTIONAL MATCH (p)-[:RELATED {predicate_id: 'P29'}]->(year:Thing)
WITH cont.id AS contributionId, cont.label AS label, p.label AS title, year.label AS year, p.id AS paperId
RETURN contributionId, label, title, year, paperId
""",
    countQuery = """UNWIND $ids AS id MATCH (r:Resource {id: id}) RETURN COUNT(r) AS cnt""")
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<Neo4jContributionInfo>
}

@QueryResult
data class Neo4jContributionInfo(
    val contributionId: String,
    val label: String,
    val title: String,
    val year: String?,
    val paperId: String
) {
    fun toContributionInfo() =
        ContributionInfo(
            id = ThingId(contributionId),
            label = label,
            paperTitle = title,
            paperYear = year?.toInt(),
            paperId = ThingId(paperId)
        )
}
