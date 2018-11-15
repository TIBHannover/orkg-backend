package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long> {
    override fun findAll(): Iterable<Neo4jLiteral>

    override fun findById(id: Long?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String): Iterable<Neo4jLiteral>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jLiteral>
}
