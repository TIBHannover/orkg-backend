package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.ObjectService.Constants.ID_DOI_PREDICATE
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

private const val id = "${'$'}id"

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long> {
    fun existsByLiteralId(id: LiteralId): Boolean

    fun findByLiteralId(id: LiteralId?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String, pageable: Pageable): Page<Neo4jLiteral>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jLiteral>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jLiteral>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: 'P31'}]->(:Resource {resource_id: $id}), (n)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(L:Literal) RETURN L""")
    fun findDOIByContributionId(id: ResourceId): Optional<Neo4jLiteral>
}
