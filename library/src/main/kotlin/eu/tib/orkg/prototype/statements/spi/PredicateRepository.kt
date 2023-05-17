package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PredicateRepository : EntityRepository<Predicate, ThingId> {
    // legacy methods:
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Predicate>
    // TODO: Work-around for https://jira.spring.io/browse/DATAGRAPH-1200. Replace with IgnoreCase or ContainsIgnoreCase when fixed.
    fun findByPredicateId(id: ThingId): Optional<Predicate>
    fun deleteByPredicateId(id: ThingId)
    fun deleteAll()
    fun save(predicate: Predicate)
    fun nextIdentity(): ThingId
}
