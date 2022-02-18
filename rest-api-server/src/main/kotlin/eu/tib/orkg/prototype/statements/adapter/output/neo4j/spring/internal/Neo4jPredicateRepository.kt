package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, Long> {
    @Deprecated("Migrate to the pageable version.")
    override fun findAll(): Iterable<Neo4jPredicate>

    override fun findAll(pageable: Pageable): Page<Neo4jPredicate>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jPredicate>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jPredicate>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jPredicate>

    fun findByPredicateId(id: PredicateId?): Optional<Neo4jPredicate>
}