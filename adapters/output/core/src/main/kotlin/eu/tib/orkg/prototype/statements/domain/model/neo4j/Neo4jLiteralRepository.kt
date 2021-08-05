package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.constants.ID_DOI_PREDICATE
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import java.util.Optional

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long> {
    override fun findAll(): List<Neo4jLiteral>

    override fun findById(id: Long?): Optional<Neo4jLiteral>

    fun findByLiteralId(id: LiteralId?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String): Iterable<Neo4jLiteral>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jLiteral>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jLiteral>

    @Query("""MATCH (n:Paper)-[:RELATED {predicate_id: 'P31'}]->(:Resource {resource_id: {0}}), (n)-[:RELATED {predicate_id: "$ID_DOI_PREDICATE"}]->(L:Literal) RETURN L""")
    fun findDOIByContributionId(id: ResourceId): Optional<Neo4jLiteral>
}
