package org.orkg.contenttypes.adapter.output.neo4j.internal

import org.orkg.common.ThingId
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

private const val PAGE_PARAMS = ":#{orderBy(#pageable)} SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jPaperRepository : Neo4jRepository<Neo4jResource, ThingId> {

    @Query("""
MATCH (r:Resource {id: $id})
CALL apoc.path.expandConfig(r, {relationshipFilter: "<RELATED", labelFilter: "/Paper", uniqueness: "RELATIONSHIP_GLOBAL"})
YIELD path
WITH last(nodes(path)) AS paper, reverse(apoc.coll.flatten([r in relationships(path) | [r, startNode(r)]])) AS path
UNWIND path AS thing
MATCH (t:Thing)
WHERE t.id = thing.id
RETURN paper, COLLECT(t) AS path $PAGE_PARAMS""",
        countQuery = """
MATCH (r:Resource {id: $id})
CALL apoc.path.expandConfig(r, {relationshipFilter: "<RELATED", labelFilter: "/Paper", uniqueness: "RELATIONSHIP_GLOBAL"})
YIELD path
WITH last(nodes(path)) AS paper
RETURN COUNT(DISTINCT paper) AS cnt""")
    fun findAllPapersRelatedToResource(id: ThingId, pageable: Pageable): Page<Neo4jPaperWithPath>
}

data class Neo4jPaperWithPath(
    val paper: Neo4jResource,
    val path: List<Neo4jThing>
)
