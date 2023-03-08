package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val id = "${'$'}id"

private const val PAGE_PARAMS = "SKIP ${'$'}skip LIMIT ${'$'}limit"

interface Neo4jPaperRepository : Neo4jRepository<Neo4jResource, Long> {

    @Query(value = """MATCH p=(paper:Paper)-[:RELATED*]->(:Resource{resource_id: $id}) WITH paper, apoc.coll.flatten([r in relationships(p) | [startNode(r), r]]) AS path UNWIND path AS thing MATCH (t:Thing) WHERE t.resource_id = thing.resource_id OR t.predicate_id = thing.predicate_id RETURN paper, COLLECT(t) AS path $PAGE_PARAMS""",
        countQuery = """MATCH p=(paper:Paper)-[:RELATED*]->(:Resource{resource_id: $id}) WITH paper, apoc.coll.flatten([r in relationships(p) | [startNode(r), r]]) AS path UNWIND path AS thing MATCH (t:Thing) WHERE t.resource_id = thing.resource_id OR t.predicate_id = thing.predicate_id RETURN COUNT(DISTINCT paper) AS cnt""")
    fun findAllPapersRelatedToResource(id: ResourceId, pageable: Pageable): Page<Neo4jPaperWithPath>
}

data class Neo4jPaperWithPath(
    val paper: Neo4jResource,
    val path: List<Neo4jThing>
)
