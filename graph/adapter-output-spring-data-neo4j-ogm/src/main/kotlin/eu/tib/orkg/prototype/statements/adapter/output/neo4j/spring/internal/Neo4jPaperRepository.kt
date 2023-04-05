package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.annotation.QueryResult
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jPaperRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query("""
MATCH (r:Resource {resource_id: $id})
CALL apoc.path.expandConfig(r, {relationshipFilter: "<RELATED", labelFilter: "/Paper", uniqueness: "RELATIONSHIP_GLOBAL"})
YIELD path
WITH last(nodes(path)) AS paper, apoc.coll.reverse(apoc.coll.flatten([r in relationships(path) | [r, startNode(r)]])) AS path
UNWIND path AS thing
MATCH (t:Thing)
WHERE t.resource_id = thing.resource_id OR t.predicate_id = thing.predicate_id
RETURN paper, COLLECT(t) AS path""",
        countQuery = """
MATCH (r:Resource {resource_id: $id})
CALL apoc.path.expandConfig(r, {relationshipFilter: "<RELATED", labelFilter: "/Paper", uniqueness: "RELATIONSHIP_GLOBAL"})
YIELD path
WITH last(nodes(path)) AS paper
RETURN COUNT(DISTINCT paper) AS cnt""")
    fun findAllPapersRelatedToResource(id: ResourceId, pageable: Pageable): Page<Neo4jPaperWithPath>
}

@QueryResult
data class Neo4jPaperWithPath(
    val paper: Neo4jResource,
    val path: List<Neo4jThing>
)
