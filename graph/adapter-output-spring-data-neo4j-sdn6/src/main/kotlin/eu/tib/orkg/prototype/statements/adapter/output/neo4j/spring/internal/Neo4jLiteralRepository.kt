package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, LiteralId> {
    fun existsByLiteralId(id: LiteralId): Boolean

    fun findByLiteralId(id: LiteralId?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String, pageable: Pageable): Page<Neo4jLiteral>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jLiteral>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jLiteral>
}