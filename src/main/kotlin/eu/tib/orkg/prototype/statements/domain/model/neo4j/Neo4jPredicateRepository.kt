package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.springframework.data.neo4j.repository.*
import java.util.*

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, Long> {
    override fun findAll(): Iterable<Neo4jPredicate>

    override fun findById(id: Long?): Optional<Neo4jPredicate>

    fun findAllByLabel(label: String): Iterable<Neo4jPredicate>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String): Iterable<Neo4jPredicate>

    fun findAllByLabelContaining(part: String): Iterable<Neo4jPredicate>

    fun findByPredicateId(id: PredicateId?): Optional<Neo4jPredicate>
}
