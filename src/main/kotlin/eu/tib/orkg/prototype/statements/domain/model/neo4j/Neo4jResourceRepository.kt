package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import java.util.Optional

interface Neo4jResourceRepository : Neo4jRepository<Neo4jResource, Long> {
    override fun findAll(): Iterable<Neo4jResource>

    override fun findById(id: Long?): Optional<Neo4jResource>

    fun findByResourceId(id: ResourceId?): Optional<Neo4jResource>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jResource>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jResource>

    // TODO: limit the selection of sortBy values to (label & id) only because they are explicit in the query
    @Query(value = """MATCH (node:`Resource`) WHERE {0} IN labels(node) WITH node, node.label AS label, node.resource_id AS id RETURN node""",
        countQuery = """MATCH (node:`Resource`) WHERE {0} IN labels(node) WITH COUNT(node) as cnt RETURN cnt""")
    fun findAllByClass(`class`: String, pageable: Pageable): Page<Neo4jResource>
}
