package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface PredicateRepository {

    fun nextIdentity(): PredicateId

    fun save(predicate: Predicate)

    fun findAll(pageable: Pageable): Page<Predicate>

    /**
     * Find a predicate by its ID.
     */
    fun findById(id: PredicateId?): Optional<Predicate>

    /**
     * Find all predicates matching a label.
     */
    fun findAllByLabelExactly(label: String, pageable: Pageable): Page<Predicate>

    /**
     * Find all predicates matching a label partially.
     */
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Predicate>
}
