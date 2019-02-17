package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jLiteralRepository : Neo4jRepository<Neo4jLiteral, Long>, Neo4jLiteralRepositoryCustom {
    override fun findAll(): Iterable<Neo4jLiteral>

    override fun findById(id: Long?): Optional<Neo4jLiteral>

    fun findByLiteralId(id: LiteralId?): Optional<Neo4jLiteral>

    fun findAllByLabel(value: String): Iterable<Neo4jLiteral>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jLiteral>
}

interface Neo4jLiteralRepositoryCustom : IdentityGenerator<LiteralId>

class Neo4jLiteralRepositoryCustomImpl : Neo4jLiteralRepositoryCustom {
    var counter = 0L

    override fun nextIdentity(): LiteralId {
        counter++
        return LiteralId(counter)
    }
}
