package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.UUID

interface Neo4jStatsRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""CALL apoc.meta.stats()""")
    fun getGraphMetaData(): Iterable<HashMap<String, Any>>

    @Query("""MATCH (n:Resource{resource_id: 'R11'}) CALL apoc.path.subgraphAll(n, {relationshipFilter: '>'}) YIELD nodes UNWIND nodes as field WITH COUNT(field) as cnt RETURN cnt""")
    fun getResearchFieldsCount(): Long

    @Query("""MATCH (n:ResearchField) WITH n OPTIONAL MATCH (n)-[:RELATES_TO*0..3 {predicate_id: 'P36'}]->(:ResearchField)<-[:RELATES_TO {predicate_id: 'P30'}]-(p:Paper) RETURN n.resource_id AS fieldId, n.label AS field, COUNT(p) AS papers""")
    fun getResearchFieldsPapersCount(): Iterable<FieldsStats>

    @Query("""
        MATCH (p:Paper)
        WITH COLLECT(DISTINCT p.created_by) AS users
        UNWIND users AS user
        MATCH (p:Paper {created_by: user})
        RETURN user, COUNT(p) AS papers
        """)
    fun getPapersPerUserCount(): Iterable<PapersPerUser>

    @Query("""
        MATCH (p:Paper)
        WITH COLLECT(DISTINCT p.created_by) AS users, datetime() AS now
        UNWIND users AS user
        MATCH (p:Paper {created_by: user})
        WHERE datetime(p.created_at).year = now.year AND datetime(p.created_at).month = now.month
        RETURN user, COUNT(p) AS papers
        """)
    fun getPapersPerUserCountThisMonth(): Iterable<PapersPerUser>

    @Query("""
        MATCH (p:Paper)
        WITH COLLECT(DISTINCT p.created_by) AS users, datetime() AS now
        UNWIND users AS user
        MATCH (p:Paper {created_by: user})
        WHERE datetime(p.created_at).year = now.year AND datetime(p.created_at).month = now.month AND datetime(p.created_at).week = now.week
        RETURN user, COUNT(p) AS papers
        """)
    fun getPapersPerUserCountThisWeek(): Iterable<PapersPerUser>

    @Query("""
        MATCH (p:Paper)
        WITH COLLECT(DISTINCT p.created_by) AS users, datetime() AS now, range(1, datetime().month) AS months
        UNWIND months AS month
        UNWIND users AS user
        MATCH (p:Paper {created_by: user})
        WHERE datetime(p.created_at).year = now.year AND datetime(p.created_at).month = month
        RETURN user, month, COUNT(p) AS papers
        ORDER BY user, month
        """)
    fun getPapersPerUserCountMonthlyThisYear(): Iterable<Triple<String, Long, Long>>

    @Query("""
        MATCH (n:Paper {resource_id: {0}})
        CALL apoc.path.subgraphAll(n, {relationshipFilter:'>'}) 
        YIELD relationships 
        UNWIND relationships as rel
        WITH rel AS p, startNode(rel) AS s, endNode(rel) AS o, n
        RETURN n.resource_id AS paper, apoc.coll.toSet(COLLECT(DISTINCT p.created_by) + COLLECT(DISTINCT s.created_by) + COLLECT(DISTINCT s.created_by)) AS contributors
        """)
    fun getPaperContributors(paperId: ResourceId): Iterable<HashMap<ResourceId, Array<String>>>

    @Query("""
        MATCH (e:AuditableEntity)
        WITH COLLECT(DISTINCT e.created_by) AS contributors, datetime() AS now
        UNWIND contributors AS contributor
        MATCH (n:AuditableEntity {created_by: contributor})
        WHERE datetime(n.created_at).year = now.year AND datetime(n.created_at).month = now.month AND datetime(n.created_at).week = now.week
        RETURN contributor, COUNT(n) AS cnt
        ORDER BY cnt DESC
        """)
    // TODO: these calls might need to use a LIMIT if the number of results is too big
    fun getMostActiveContributorsThisWeek(): Iterable<HashMap<String, Long>>
}

@QueryResult
data class FieldsStats(
    val fieldId: String,
    val field: String,
    val papers: Long
)

@QueryResult
data class PapersPerUser(
    val user: String,
    val papers: Long
) {
    val userId: UUID
        get() = UUID.fromString(user)
}
