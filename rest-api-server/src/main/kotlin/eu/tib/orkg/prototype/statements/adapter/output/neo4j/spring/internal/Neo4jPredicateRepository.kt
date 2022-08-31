package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository

interface Neo4jPredicateRepository : Neo4jRepository<Neo4jPredicate, Long> {
    fun existsByPredicateId(id: PredicateId): Boolean

    @Deprecated("Migrate to the pageable version.")
    override fun findAll(): Iterable<Neo4jPredicate>

    override fun findAll(pageable: Pageable): Page<Neo4jPredicate>

    fun findAllByLabel(label: String, pageable: Pageable): Page<Neo4jPredicate>

    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Neo4jPredicate>

    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Neo4jPredicate>

    fun findByPredicateId(id: PredicateId?): Optional<Neo4jPredicate>

    // @Query was added manually because of a strange bug that it is not reproducible. It seems that the OGM generates
    // a query containing a string literal when the set only has one element, which the driver refused as an invalid
    // query (which it is). It only happens under certain circumstances which are not reproducible in a test. I checked
    // the driver version and everything else that came to mind. No idea what is wrong. This seems to work. -- MP
    @Query("""MATCH (n:`Predicate`) WHERE n.predicate_id in {0} RETURN n""")
    fun findAllByPredicateIdIn(ids: Set<PredicateId>): Iterable<Neo4jPredicate>
}
