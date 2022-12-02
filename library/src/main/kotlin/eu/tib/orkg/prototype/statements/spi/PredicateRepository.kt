package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PredicateRepository : EntityRepository<Predicate, PredicateId> {
    // legacy methods:
    fun findAllByLabel(label: String, pageable: Pageable): Page<Predicate>
    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Predicate>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate>
    fun findByPredicateId(id: PredicateId?): Optional<Predicate>
    fun deleteByPredicateId(id: PredicateId)
    fun deleteAll()
    fun save(predicate: Predicate)
    fun nextIdentity(): PredicateId
    fun usageCount(id: PredicateId): Long
}
