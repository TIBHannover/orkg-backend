package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, Long> {
    override fun findAll(): Iterable<Neo4jPredicate>

    override fun findById(id: Long?): Optional<Neo4jPredicate>

    fun findAllByLabel(label: String): Iterable<Neo4jPredicate>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jPredicate>
}
